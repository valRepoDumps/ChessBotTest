// Main process: all UI state and logic. The renderer only draws the "view" we push
// and forwards user intents through the single 'ui' IPC channel.
//
// createGame(backend, send)
//   backend: async functions that talk to the external chess application
//            (requestMove, requestComputerMove, getValidMoves, newGame, saveGame, loadGame,
//             listSaves, deleteSave, undoTurn, listEngines, useEngine,
//             listCurrentEngineOptions, setEngineOption, setEnginePreset)
//   send:    (channel, data) => webContents.send(channel, data)

import { START_FEN, EMPTY_FEN, parseFen, flagsFromParsed, buildFen, validateSetup } from '../common/fenUtils.js';

const COLORS = { green: '#4caf50', red: '#e5484d', blue: '#3c7fd6' };
const NO_PROMOTION = ""; //no promo is empty string, cause null will break backend. . 
const GAME_OVER_TEXT = { draw: 'draw', w: 'white won', b: 'black won' };
const EDIT_HINT = 'Pick a piece, then click squares. Click an occupied square with the same piece to remove it.';

export function createGame(backend, send) {
  const S = {
    board: {},              // square -> piece code, e.g. 'wP'
    highlights: {},         // square -> css color
    selected: null,
    validDestinations: [],
    selectToken: 0,         // invalidates in-flight getValidMoves calls
    busy: false,
    currentFen: START_FEN,  // last position confirmed by the engine
    gameOver: false,
    mode: 'play',           // 'play' | 'edit'
    tool: null,             // piece code, 'erase', or null
    computerSide: false,    // false | 'w' | 'b'
    panel: null,            // 'editor' | 'saves' | 'vs-computer' | null
    msg: '',
    connected: false,
    flags: flagsFromParsed(parseFen(START_FEN)),
    promotion: null,        // { from, to, color } while the picker is open
    saves: { ok: true, loading: true },
    saveNameRev: 0,         // bumped after a successful save so the renderer clears the name box
    engines: { loading: true },
    usedEngineKey: null,
    advancedOpen: false,
    options: { rev: 0, visible: false, presets: [], advanced: [] },
  };
  const pendingOptions = new Map();   // option name -> new value, sent by Apply

  // ---------- view ----------
  function buildView() {
    return {
      board: S.board, highlights: S.highlights, selected: S.selected,
      busy: S.busy, mode: S.mode, tool: S.tool, msg: S.msg, connected: S.connected,
      panel: S.panel, computerSide: S.computerSide, flags: S.flags,
      fenText: S.mode === 'edit' ? buildFen(S.board, S.flags) : null,
      promotion: S.promotion,
      saves: S.saves, saveNameRev: S.saveNameRev,
      engines: engineView(), options: S.options, advancedOpen: S.advancedOpen,
    };
  }
  const push = () => send('view', buildView());

  function engineView() {
    const r = S.engines;
    if (r.loading || !r.ok) return { loading: !!r.loading, ok: r.ok, reason: r.reason };
    return {
      ok: true,
      list: r.engines.map((e) => ({
        name: e.name || e.path || 'Engine',
        author: e.author || '',
        inUse: JSON.stringify(e) === S.usedEngineKey,
      })),
    };
  }

  // ---------- helpers ----------
  async function runBusy(label, fn) {
    if (S.busy) return null;
    S.busy = true;
    if (label) S.msg = label;
    push();
    try { return await fn(); }
    finally {
      S.busy = false;
      if (label && S.msg === label) S.msg = '';   // only clear our own label
      push();
    }
  }

  function applyFen(fen) {
    const p = parseFen(fen);
    if (!p) return false;
    S.board = p.board;
    S.flags = flagsFromParsed(p);
    return true;
  }

  function clearSelection() {
    S.selectToken++;
    S.selected = null;
    S.highlights = {};
    S.validDestinations = [];
  }

  // ---------- messages pushed by the backend ----------
  function onUpdateBoard(msg) {
    if (!parseFen(msg.fen)) { S.msg = 'Received an invalid board from the engine'; return push(); }

    S.currentFen = msg.fen;
    if (S.mode === 'edit' && !msg.is_new_game) return;   // don't overwrite an in-progress edit
    applyFen(msg.fen);
    clearSelection();

    const over = msg.game_over;   // a missing field means the game is still running
    S.gameOver = !!over && over !== 'indeterminate';
    if (S.gameOver) S.msg = `Game over: ${GAME_OVER_TEXT[over] ?? over}`;

    if (msg.is_new_game) closeEditor(false);
    else push();
  }

  function onNotice(text) { if (text) { S.msg = text; push(); } }

  function onSaves(msg) {
    S.saves = { ...msg, loading: false };
    if (msg.saved) S.saveNameRev++;
    push();
  }

  // When the engine first connects (or reconnects after dropping), ask it for a fresh game.
  function onConnection(ok) {
    const was = S.connected;
    S.connected = ok;
    push();
    if (ok && !was) runBusy('Starting new game…', () => backend.newGame({ fen: START_FEN }));
  }

  // ---------- editor ----------
  function openEditor() {
    clearSelection();
    applyFen(S.currentFen);
    S.mode = 'edit';
    S.tool = null;
    S.panel = 'editor';
    S.msg = EDIT_HINT;
    push();
  }

  function closeEditor(restore) {
    if (restore) { applyFen(S.currentFen); S.msg = ''; }
    S.mode = 'play';
    S.panel = null;
    push();
  }

  function togglePanel(name, onOpen) {
    if (S.mode === 'edit') closeEditor(true);
    if (S.panel !== name) { S.panel = name; push(); onOpen(); }
    else { S.panel = null; push(); }
  }

  async function startGame() {
    const err = validateSetup(S.board, S.flags);
    if (err) { S.msg = err; return push(); }
    await runBusy('Starting new game…', () => backend.newGame({ fen: buildFen(S.board, S.flags) }));
  }

  // ---------- selection & moves ----------
  const isPromotion = (from, to) => {
    const p = S.board[from];
    return p?.[1] === 'P' && to[1] === (p[0] === 'w' ? '8' : '1');
  };

  async function selectSquare(name) {
    clearSelection();
    const token = S.selectToken;
    S.selected = name;
    S.highlights[name] = COLORS.blue;
    push();   // instant feedback

    let moves = [];
    try { moves = await backend.getValidMoves(name); }
    catch (e) { console.error(e); }

    if (token !== S.selectToken) return;   // user already clicked elsewhere
    S.validDestinations = Array.isArray(moves) ? moves : [];
    for (const m of S.validDestinations) {
      if (m.to) S.highlights[m.to] = m.captured ? COLORS.red : COLORS.green;
    }
    push();
  }

  async function onSquareClick({ sq }) {
    if (S.mode === 'edit') {
      if (S.tool === 'erase' || S.board[sq] === S.tool) delete S.board[sq];
      else if (S.tool) S.board[sq] = S.tool;
      return push();
    }
    if (S.busy) return;

    if (S.selected === sq) { clearSelection(); return push(); }

    if (S.selected && S.validDestinations.some((m) => m.to === sq)) {
      const from = S.selected;
      clearSelection();
      return tryMove(from, sq);
    }

    if (S.board[sq]) return selectSquare(sq);
    clearSelection();
    push();
  }

  async function tryMove(from, to) {
    if (from === to || !S.board[from]) { clearSelection(); return push(); }
    if (isPromotion(from, to)) {   // the renderer shows the picker and answers with a 'promotion' intent
      S.promotion = { from, to, color: S.board[from][0] };
      return push();
    }
    await submitMove(from, to, NO_PROMOTION);
  }

  async function submitMove(from, to, promotion) {
    const r = await runBusy(`Waiting for confirmation of ${from} → ${to}…`,
      () => backend.requestMove({ from, to, promotion, computer_side: S.computerSide }));
    if (r?.ok) await maybeComputerMove();
  }

  async function onPromotion({ piece }) {
    const p = S.promotion;
    if (!p) return;
    S.promotion = null;
    if (!piece) return push();   // picker cancelled
    await submitMove(p.from, p.to, piece);
  }

  // Second half of a computer turn. Does nothing if computer mode is off or the game is over.
  async function maybeComputerMove() {
    if (!S.computerSide || S.gameOver) return;
    await runBusy('Computer is thinking…',
      () => backend.requestComputerMove({ computer_side: S.computerSide }));
  }

  // ---------- saves ----------
  function refreshSaves() {
    S.saves = { ok: true, loading: true };
    push();
    backend.listSaves();   // result arrives through onSaves
  }

  async function loadGame({ save_id }) {
    const r = await runBusy('Loading…',
      () => backend.loadGame({ save_id, computer_side: S.computerSide }));
    if (r?.ok) await maybeComputerMove();
  }

  // ---------- computer side & engines ----------
  function setComputer({ on, side }) {
    S.computerSide = on ? side : false;
    clearSelection();
    push();
    maybeComputerMove();   // if it's already the computer's turn it plays now
  }

  async function refreshEngines() {
    S.engines = { loading: true };
    push();
    const r = await backend.listEngines();
    if (!S.usedEngineKey && r.ok) {
      const builtin = r.engines.find((e) => e.builtin);   // built-in engine is the default
      if (builtin) S.usedEngineKey = JSON.stringify(builtin);
    }
    S.engines = r;
    push();
  }

  async function useEngine({ index }) {
    const engine = S.engines.engines?.[index];
    if (!engine) return;
    const r = await runBusy('Switching engine…', () => backend.useEngine({ engine }));
    if (r?.ok) {
      S.usedEngineKey = JSON.stringify(engine);
      push();
      await refreshEngineOptions();
    }
  }

  async function refreshEngineOptions() {
    const r = await backend.listCurrentEngineOptions({});
    const presets = (r?.presets || []).filter((p) => p.supported);
    const advanced = r?.advanced || [];
    pendingOptions.clear();
    S.options = {
      rev: S.options.rev + 1,
      visible: !!(r?.ok && (presets.length || advanced.length)),
      presets, advanced,
    };
    push();
  }

  async function applyAdvanced() {
    if (!pendingOptions.size) { S.msg = 'No advanced option changed'; return push(); }
    await runBusy('Applying options…', async () => {
      for (const [property, value] of pendingOptions) {
        const r = await backend.setEngineOption({ property, value });
        if (!r?.ok) break;   // the failure notice is already shown; stop here
      }
    });
    refreshEngineOptions();   // re-sync the controls with the engine's real values
  }

  async function sendOption({ name }) {   // option buttons
    const r = await runBusy(null, () => backend.setEngineOption({ property: name, value: undefined }));
    if (!r?.ok) refreshEngineOptions();
  }

  async function applyPreset({ preset, values }) {
    const r = await runBusy('Applying…', () => backend.setEnginePreset({ preset, values }));
    if (!r?.ok) refreshEngineOptions();
  }

  // ---------- intent dispatch ----------
  const handlers = {
    ready: () => push(),

    // board
    square: onSquareClick,
    dragstart: () => { clearSelection(); },   // no push: re-rendering mid-drag cancels the drag
    drop: ({ from, to }) => (from && S.mode === 'play' && !S.busy ? tryMove(from, to) : undefined),
    promotion: onPromotion,
    undo: () => runBusy('Undoing turn(s)',
      () => backend.undoTurn({ turns: String(S.computerSide ? 2 : 1) })),

    // editor
    'editor-open': openEditor,
    'editor-cancel': () => closeEditor(true),
    'editor-start': startGame,
    'editor-clear': () => { applyFen(EMPTY_FEN); push(); },
    'editor-initial': () => { applyFen(START_FEN); push(); },
    'editor-apply-fen': ({ fen }) => { S.msg = applyFen(fen) ? '' : 'Invalid FEN'; push(); },
    'editor-flags': ({ flags }) => { S.flags = flags; push(); },
    'editor-tool': ({ tool }) => { S.tool = S.tool === tool ? null : tool; push(); },

    // panels
    'toggle-saves': () => togglePanel('saves', refreshSaves),
    'toggle-computer': () => togglePanel('vs-computer', () => refreshEngines().then(refreshEngineOptions)),

    // saves
    'refresh-saves': refreshSaves,
    save: ({ name }) => runBusy(null, () => backend.saveGame({ name: name || 'Untitled' })),
    load: loadGame,
    'delete-save': ({ save_id }) => runBusy('Deleting…', () => backend.deleteSave({ save_id })),

    // vs computer / engines
    computer: setComputer,
    'refresh-engines': refreshEngines,
    'use-engine': useEngine,
    'toggle-advanced': () => { S.advancedOpen = !S.advancedOpen; push(); },
    'option-changed': ({ name, value }) => { pendingOptions.set(name, value); },
    'apply-advanced': applyAdvanced,
    'send-option': sendOption,
    'apply-preset': applyPreset,
  };

  async function handle(intent) {
    const fn = handlers[intent?.type];
    return fn ? fn(intent) : undefined;
  }

  applyFen(START_FEN);

  return { handle, onUpdateBoard, onNotice, onSaves, onConnection };
}