const FILES = 'abcdefgh';
const GLYPH = { K: '♚', Q: '♛', R: '♜', B: '♝', N: '♞', P: '♟' };
const COLORS = { green: '#4caf50', red: '#e5484d', blue: '#3c7fd6' };
const START_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';
const EMPTY_FEN = '8/8/8/8/8/8/8/8 w - - 0 1';
const INVALID_PROMOTION = "false";

const $ = (id) => document.getElementById(id);
const boardEl = $('board');
const msgEl = $('msg');

// ================= State =================
const board = {};               // square -> piece code, e.g. 'wP'
const highlights = new Map();   // square -> css color
let selected = null;
let validDestinations = [];
let selectToken = 0;            // invalidates in-flight getValidMoves calls
let busy = false;
let currentFen = START_FEN;     // last position confirmed by the engine
let gameOver = false;

let mode = 'play';              // 'play' | 'edit'
let tool = null;                // piece code, 'erase', or null

//allow for playing with computers. 
const UNDO_TURNS = { normal: 1, computer: 2 };
const INVALID_SIDE = false;
let computerSide = INVALID_SIDE;   // false = manual | 'w' | 'b' = side the computer plays

// ================= FEN helpers =================
function parseFen(fen) {
  if (typeof fen !== 'string') return null;
  const [placement, turn = 'w', castling = '-', ep = '-', half = '0', full = '1'] = fen.trim().split(/\s+/);
  const rows = (placement || '').split('/');
  if (rows.length !== 8) return null;
  const b = {};
  for (let i = 0; i < 8; i++) {
    let f = 0;
    for (const ch of rows[i]) {
      if (/[1-8]/.test(ch)) f += Number(ch);
      else if (/[pnbrqkPNBRQK]/.test(ch) && f < 8) {
        b[FILES[f] + (8 - i)] = (ch === ch.toUpperCase() ? 'w' : 'b') + ch.toUpperCase();
        f++;
      } else return null;
    }
    if (f !== 8) return null;
  }
  return { board: b, turn, castling, ep, half: Number(half) || 0, full: Number(full) || 1 };
}

function placementFen() {
  const rows = [];
  for (let r = 8; r >= 1; r--) {
    let row = '', empty = 0;
    for (let f = 0; f < 8; f++) {
      const p = board[FILES[f] + r];
      if (!p) { empty++; continue; }
      if (empty) { row += empty; empty = 0; }
      row += p[0] === 'w' ? p[1] : p[1].toLowerCase();
    }
    rows.push(row + (empty || ''));
  }
  return rows.join('/');
}

function readFlags() {
  const castling = ['K', 'Q', 'k', 'q'].filter((c) => $('e-c' + c).checked).join('') || '-';
  return {
    turn: $('e-turn').value,
    castling,
    en_passant: $('e-ep').value.trim() || '-',
    halfmove: Number($('e-half').value) || 0,
    fullmove: Number($('e-full').value) || 1,
  };
}

function buildFen() {
  const f = readFlags();
  return `${placementFen()} ${f.turn} ${f.castling} ${f.en_passant} ${f.halfmove} ${f.fullmove}`;
}

// Sets the board AND the editor's flag inputs from a FEN.
function applyFen(fen) {
  const p = parseFen(fen);
  if (!p) return false;
  for (const sq in board) delete board[sq];
  Object.assign(board, p.board);
  $('e-turn').value = p.turn === 'b' ? 'b' : 'w';
  for (const c of ['K', 'Q', 'k', 'q']) $('e-c' + c).checked = p.castling.includes(c);
  $('e-ep').value = p.ep;
  $('e-half').value = p.half;
  $('e-full').value = p.full;
  return true;
}

function validateSetup() {
  const codes = Object.entries(board);
  for (const k of ['wK', 'bK']) {
    if (codes.filter(([, c]) => c === k).length !== 1) return `Each side needs exactly one king (${k} count is wrong)`;
  }
  if (codes.some(([sq, c]) => c[1] === 'P' && (sq[1] === '1' || sq[1] === '8'))) return 'Pawns cannot sit on rank 1 or 8';
  const ep = readFlags().en_passant;
  if (ep !== '-' && !/^[a-h][36]$/.test(ep)) return 'En passant must be "-" or a square on rank 3 or 6';
  return null;
}

// =============VS Computer==================

// In computer mode you may only pick up your own pieces.
// You can't pick up the computer's pieces.
function canControl(square) {
  return true; //true regardless of verything. 
  // return !computerSide || board[square]?.[0] !== computerSide;
}

function updateComputerButton() {
  const btn = $('btn-computer');
  btn.classList.toggle('active', !!computerSide);
  btn.textContent = 'vs Computer: ' +
    (computerSide === 'w' ? 'plays White' : computerSide === 'b' ? 'plays Black' : 'Off');
}

// Panel controls -> computerSide, the one value the rest of the code already reads.
function applyComputerSettings() {
  computerSide = $('vc-on').checked ? $('vc-side').value : INVALID_SIDE;
  $('vc-options').disabled = !computerSide;   // greys out side + engines while off
  clearSelection();
  updateComputerButton();
  render();
  maybeComputerMove();   // from my last message; if it's already the computer's turn it plays now
}

// ================= Rendering =================
function render() {
  if (mode === 'edit') $('e-fen').value = buildFen();
  boardEl.innerHTML = '';
  boardEl.classList.toggle('busy', busy);
  $('btn-undo').disabled = busy || mode !== 'play';

  for (let r = 8; r >= 1; r--) {
    for (let f = 0; f < 8; f++) {
      const name = FILES[f] + r;
      const sq = document.createElement('div');
      sq.className = 'sq ' + ((f + r) % 2 === 1 ? 'dark' : 'light');
      sq.dataset.sq = name;
      if (name === selected) sq.classList.add('selected');

      if (highlights.has(name)) {
        const hl = document.createElement('div');
        hl.className = 'hl';
        hl.style.background = highlights.get(name);
        sq.appendChild(hl);
      }
      if (f === 0) sq.insertAdjacentHTML('beforeend', `<span class="coord rank">${r}</span>`);
      if (r === 1) sq.insertAdjacentHTML('beforeend', `<span class="coord file">${FILES[f]}</span>`);

      const code = board[name];
      if (code) {
        const piece = document.createElement('div');
        piece.className = 'piece ' + code[0];
        piece.textContent = GLYPH[code[1]] + '\uFE0E';
        piece.draggable = !busy && mode === 'play';
        sq.appendChild(piece);
      }
      boardEl.appendChild(sq);
    }
  }
}

function isPromotion(from, to) {
  const p = board[from];
  return p?.[1] === 'P' && to[1] === (p[0] === 'w' ? '8' : '1');
}

function pickPromotion(color) {
  return new Promise((resolve) => {
    const overlay = document.createElement('div');
    overlay.className = 'promo-overlay';
    const box = document.createElement('div');
    box.className = 'promo-box';

    const onKey = (e) => { if (e.key === 'Escape') done(null); };
    const done = (value) => {
      document.removeEventListener('keydown', onKey);
      overlay.remove();
      resolve(value);
    };

    for (const t of ['Q', 'R', 'B', 'N']) {
      const b = document.createElement('button');
      b.className = color;                         // 'w' or 'b', same styling as the palette
      b.textContent = GLYPH[t] + '\uFE0E';
      b.addEventListener('click', () => done(t));
      box.appendChild(b);
    }
    overlay.addEventListener('click', (e) => { if (e.target === overlay) done(null); });
    document.addEventListener('keydown', onKey);
    overlay.appendChild(box);
    document.body.appendChild(overlay);
  });
}

// One set of delegated listeners instead of 3 per square on every render.
boardEl.addEventListener('click', (e) => {
  const sq = e.target.closest('.sq');
  if (sq) onSquareClick(sq.dataset.sq);
});

boardEl.addEventListener('dragstart', (e) => {
  const sq = e.target.closest('.sq');

  //only allow if mode 'play', not busy, and is of side player can control. 
   if (!sq || mode !== 'play' || busy || !canControl(sq.dataset.sq)) { e.preventDefault(); return; }
  e.dataTransfer.setData('text/plain', sq.dataset.sq);
  clearSelection();   // no render here: re-rendering mid-drag cancels the drag
});

boardEl.addEventListener('dragover', (e) => e.preventDefault());

boardEl.addEventListener('drop', (e) => {
  e.preventDefault();
  const sq = e.target.closest('.sq');
  const from = e.dataTransfer.getData('text/plain');
  if (sq && from && mode === 'play') tryMove(from, sq.dataset.sq);
});

// ================= Busy wrapper =================
//stop user from sending request if another request is already sent. 
async function runBusy(label, fn) {
  if (busy) return null;
  busy = true;
  if (label) msgEl.textContent = label;
  render();
  try { return await fn(); }
  finally { 
    busy = false; 
    if (label && msgEl.textContent === label) msgEl.textContent = ''; //unset the label if message no longer run. 
    render(); }
}

// ================= Selection & moves =================
function clearSelection() {
  selectToken++;
  selected = null;
  highlights.clear();
  validDestinations = [];
}

async function selectSquare(name) {
  clearSelection();
  const token = selectToken;
  selected = name;
  highlights.set(name, COLORS.blue);
  render();                                   // instant feedback

  let moves = [];
  try { moves = await window.chess.getValidMoves(name); }
  catch (e) { console.error(e); }

  if (token !== selectToken) return;          // user already clicked elsewhere
  validDestinations = Array.isArray(moves) ? moves : [];
  for (const m of validDestinations) {
    if (m.to) highlights.set(m.to, m.captured ? COLORS.red : COLORS.green);
  }
  render();
}

async function onSquareClick(name) {
  if (mode === 'edit') {
    if (tool === 'erase' || board[name] === tool) delete board[name];
    else if (tool) board[name] = tool;
    render();
    return;
  }
  if (busy) return;

  if (selected === name) { clearSelection(); render(); return; }

  if (selected !== null && validDestinations.some((m) => m.to === name)) {
    const from = selected;
    clearSelection();
    await tryMove(from, name);
    return;
  }

  //only allow the side the player control to control the piece. 
  if (board[name] && canControl(name)) await selectSquare(name);
  else { clearSelection(); render(); }
}

// main.js pushes the new board and the notice; this only locks the UI while waiting.
async function tryMove(from, to) {
  if (from === to || !board[from] || !canControl(from) ) { clearSelection(); render(); return; }

  let promotion = INVALID_PROMOTION;
  if (isPromotion(from, to)){
    promotion = await pickPromotion(board[from][0]);
    if (!promotion) { render(); return; } 
  }

  const r = await runBusy(`Waiting for confirmation of ${from} → ${to}…`,
    () => window.chess.requestMove({ from, to, promotion, computer_side: computerSide }));

  if (r?.ok) await maybeComputerMove();
}

// Second step of a computer turn. The user's move is already on screen; this asks for the reply,
// which arrives as a second update_board. The backend does nothing if it isn't the computer's turn.
async function maybeComputerMove() {
  if (!computerSide) return;
  await runBusy('Computer is thinking…',
    () => window.chess.requestComputerMove({ computer_side: computerSide }));
}

// ================= Engines =================
let engineResult = null;     // last list from the backend
let usedEngineKey = null;    // JSON of the engine the backend confirmed

async function refreshEngines() {
  renderEngines({ ok: true, engines: [], loading: true });
  engineResult = await window.chess.listEngines();
  if (!usedEngineKey && engineResult.ok) {
    const mine = engineResult.engines.find((e) => e.builtin);
    if (mine) usedEngineKey = JSON.stringify(mine);   // MyEngine is the default
  }
  renderEngines(engineResult);
}

function renderEngines({ ok, reason, engines, loading }) {
  const ul = $('engine-list');

  ul.replaceChildren();

  const note = (text) => {
    const li = document.createElement('li');
    li.className = 'empty';
    li.textContent = text;
    ul.appendChild(li);
  };

  if (loading) return note('Loading…');
  if (!ok) return note(`Could not load engines: ${reason}`);
  if (!engines.length) return note('No engines found.');

  for (const e of engines) {
    const li = document.createElement('li');
    const info = document.createElement('div');
    const title = document.createElement('span');
    title.textContent = e.name || e.path || 'Engine';
    const small = document.createElement('small');
    small.textContent = e.author || '';
    info.append(title, small);

    const btn = document.createElement('button');
    const inUse = JSON.stringify(e) === usedEngineKey;
    btn.textContent = inUse ? 'In use' : 'Use';
    btn.disabled = inUse;
    btn.addEventListener('click', () => useEngine(e));

    li.append(info, btn);
    ul.appendChild(li);
  }
}

async function useEngine(engine) {
  const r = await runBusy('Switching engine…', () => window.chess.useEngine({ engine }));
  if (r?.ok) {
    usedEngineKey = JSON.stringify(engine);
    renderEngines(engineResult);
    await refreshEngineOptions();
  }
}

// ================= Engine options =================
let advancedOpen = false;

async function refreshEngineOptions() {
  renderEngineOptions(await window.chess.listCurrentEngineOptions({}));
}

const pendingOptions = new Map();   // option name -> new text, sent by the Apply button

function renderEngineOptions({ ok, presets = [], advanced = [] }) {
  const usable = presets.filter((p) => p.supported);
  pendingOptions.clear();
  $('engine-options').hidden = !(ok && (usable.length || advanced.length));

  const title = document.createElement('div');
  title.className = 'section-title';
  title.textContent = 'Presets';
  const box = $('preset-options');
  box.replaceChildren(title);
  if (usable.length) box.append(...usable.map(renderPreset));
  else {
    const note = document.createElement('div');
    note.className = 'empty';
    note.textContent = 'No presets available for this engine.';
    box.append(note);
  }

  $('btn-advanced').hidden = !advanced.length;
  $('advanced-list').replaceChildren(...advanced.map(renderOption));
  $('advanced-options').hidden = !advancedOpen;
}

async function applyAdvanced() {
  if (!pendingOptions.size) { msgEl.textContent = 'No advanced option changed'; return; }
  await runBusy('Applying options…', async () => {
    for (const [property, value] of pendingOptions) {
      const r = await window.chess.setEngineOption({ property, value });
      if (!r?.ok) break;   // the failure notice is already shown; stop here
    }
  });
  refreshEngineOptions();   // re-sync the controls with the engine's real values
}

// check -> checkbox, spin -> number within min/max, combo -> select, string -> text
function makeInput(p) {
  let el;
  if (p.type === 'check') {
    el = document.createElement('input');
    el.type = 'checkbox';
    el.checked = p.value === 'true';
    return { el, read: () => String(el.checked) };
  }
  if (p.type === 'spin') {
    el = document.createElement('input');
    el.type = 'number';
    if (p.min != null) el.min = p.min;
    if (p.max != null) el.max = p.max;
    el.value = p.value ?? '';
    return { el, read: () => el.value };
  }
  if (p.type === 'combo') {
    el = document.createElement('select');
    for (const v of p.vars || []) el.add(new Option(v, v));
    el.value = p.value ?? '';
    return { el, read: () => el.value };
  }
  el = document.createElement('input');
  el.value = p.value === '<empty>' ? '' : (p.value ?? '');
  return { el, read: () => el.value };
}

function renderOption(p) {
  const row = document.createElement('div');
  row.className = 'row';
  if (p.type === 'button') {
    const b = document.createElement('button');
    b.textContent = p.name;
    b.addEventListener('click', () => sendOption(p.name));
    row.append(b);
    return row;
  }
  const { el, read } = makeInput(p);
  el.addEventListener('change', () => pendingOptions.set(p.name, read()));
  const label = document.createElement('label');
  label.append(p.name, el);
  row.append(label);
  return row;
}

// A preset is several options set together: one Apply sends all values in order.
function renderPreset(preset) {
  const box = document.createElement('div');
  const title = document.createElement('div');
  title.textContent = preset.label;
  title.className = 'section-title';
  box.append(title);

  const reads = [];
  for (const p of preset.properties) {
    const { el, read } = makeInput(p);
    const label = document.createElement('label');
    label.append(p.name, el);
    const row = document.createElement('div');
    row.className = 'row';
    row.append(label);
    box.append(row);
    reads.push(read);
  }
  const apply = document.createElement('button');
  apply.textContent = 'Apply';
  apply.addEventListener('click', () => sendPreset(preset.label, reads.map((r) => r())));
  box.append(apply);
  return box;
}

async function sendOption(property, value) {   // used by buttons
  const r = await runBusy(null, () => window.chess.setEngineOption({ property, value }));
  if (!r?.ok) refreshEngineOptions();
}

async function sendPreset(preset, values) {
  const r = await runBusy('Applying…', () => window.chess.setEnginePreset({ preset, values }));
  if (!r?.ok) refreshEngineOptions();
}

// ================= Messages from main =================
window.chess.onUpdateBoard((msg) => {
  if (typeof msg.fen !== 'string' || !parseFen(msg.fen)) {
    msgEl.textContent = 'Received an invalid board from the engine';
    return;
  }
  currentFen = msg.fen;
  if (mode === 'edit' && !msg.is_new_game) return;
  applyFen(msg.fen);
  clearSelection();

  if (msg.game_over === 'indeterminate'){
    gameOver = false;
  }else{
    gameOver = true;
    let text;
    if (msg.game_over === 'draw'){
      text = 'draw';
    }else if (
      msg.game_over === 'b'
    ){
      text = "black won";
    }else if (
      msg.game_over === 'w'
    ){
      text = "white won";
    }
    msgEl.textContent = `Game over: ${text}`;
  }

  if (msg.is_new_game) closeEditor(false);
  else render();
});

window.chess.onNotice((text) => { if (text) msgEl.textContent = text; });

window.chess.onSaves((msg) => {
  renderSaves(msg);
  if (msg.saved) $('save-name').value = '';
});

window.chess.onConnection((ok) => {
  $('dot').classList.toggle('on', ok);
  $('conn').textContent = ok ? 'External application connected' : 'Waiting for external application';
  if (ok) window.chess.getState();    // pull the current position on connect
});

// ================= Panels =================
function showPanel(which) {            // 'editor' | 'saves' | null
  $('editor').hidden = which !== 'editor';
  $('saves').hidden = which !== 'saves';
  $('vs-computer').hidden = which !== 'vs-computer';
  document.body.classList.toggle('panel-open', !!which);
}

// ================= Board editor =================
function buildPalette() {
  const pal = $('palette');
  for (const code of ['wK', 'wQ', 'wR', 'wB', 'wN', 'wP', 'bK', 'bQ', 'bR', 'bB', 'bN', 'bP']) {
    const b = document.createElement('button');
    b.className = code[0];
    b.dataset.tool = code;
    b.title = code;
    b.textContent = GLYPH[code[1]] + '\uFE0E';
    pal.appendChild(b);
  }

  const er = document.createElement('button');
  er.dataset.tool = 'erase';
  er.textContent = 'Erase';
  er.style.cssText = 'width:auto;font-size:13px;padding:0 10px';
  pal.appendChild(er);

  pal.addEventListener('click', (e) => {
    const t = e.target.closest('button')?.dataset.tool;
    if (!t) return;
    tool = tool === t ? null : t;
    for (const b of pal.children) b.classList.toggle('active', b.dataset.tool === tool);
  });
}

function openEditor() {
  clearSelection();
  applyFen(currentFen);          // sync board and flag inputs with the live game
  mode = 'edit';
  tool = null;
  for (const b of $('palette').children) b.classList.remove('active');
  showPanel('editor');
  msgEl.textContent = 'Pick a piece, then click squares. Click an occupied square with the same piece to remove it.';
  render();
}

function closeEditor(restore) {
  if (restore) applyFen(currentFen);   // discard edits: board and flags
  mode = 'play';
  showPanel(null);
  render();
}

async function startGame() {
  const err = validateSetup();
  if (err) { msgEl.textContent = err; return; }
  await runBusy('Starting new game…', () => window.chess.newGame({ fen: buildFen() }));
}

// ================= Save / load =================
function refreshSaves() {
  renderSaves({ ok: true, saves: [], loading: true });
  window.chess.listSaves();
}

async function loadGame(save_id){
  const r = await runBusy('Loading…',
    () => window.chess.loadGame({ save_id: save_id, computer_side: computerSide }));
  if (r?.ok) await maybeComputerMove();
}

function renderSaves({ ok, reason, saves, loading }) {
  const ul = $('save-list');
  ul.replaceChildren();
  const note = (text) => {
    const li = document.createElement('li');
    li.className = 'empty';
    li.textContent = text;
    ul.appendChild(li);
  };
  if (loading) return note('Loading…');
  if (!ok) return note(`Could not load saves: ${reason}`);
  if (!saves.length) return note('No saved games yet. Name this game and save it.');

  for (const s of saves) {
    const li = document.createElement('li');
    const info = document.createElement('div');
    const title = document.createElement('span');
    title.textContent = s.name || s.save_id;
    const small = document.createElement('small');
    small.textContent = s.saved_at ? new Date(s.saved_at).toLocaleString() : s.save_id;
    info.append(title, small);

    const load = document.createElement('button');
    load.textContent = 'Load';
    load.addEventListener('click', () =>
      loadGame(s.save_id));

    const del = document.createElement('button');
    del.textContent = 'Delete';
    del.addEventListener('click', () =>
      runBusy('Deleting…', () => window.chess.deleteSave({ save_id: s.save_id })));

    li.append(info, load, del);
    ul.appendChild(li);
    ul.appendChild(li);
  }
}

function saveCurrent() {
  const name = $('save-name').value.trim() || 'Untitled';
  runBusy(null, () => window.chess.saveGame({ name }));
}

// ================= Wiring =================
buildPalette();
$('btn-new').addEventListener('click', openEditor);
$('btn-undo').addEventListener('click', () =>
  runBusy('Undoing turn(s)', () => window.chess.undoTurn({ turns: String(computerSide ? 2 : 1) })));
$('btn-saves').addEventListener('click', () => {
  if (mode === 'edit') closeEditor(true);
  if ($('saves').hidden) { showPanel('saves'); refreshSaves(); } else showPanel(null);
});

$('btn-computer').addEventListener('click', () => {
  if (mode === 'edit') closeEditor(true);
  if ($('vs-computer').hidden) { showPanel('vs-computer'); refreshEngines(); } else showPanel(null);
});
$('vc-on').addEventListener('change', applyComputerSettings);
$('vc-side').addEventListener('change', applyComputerSettings);
$('btn-refresh-engines').addEventListener('click', refreshEngines);

updateComputerButton();

$('btn-cancel').addEventListener('click', () => { closeEditor(true); msgEl.textContent = ''; });
$('btn-start').addEventListener('click', startGame);
$('btn-clear').addEventListener('click', () => { applyFen(EMPTY_FEN); render(); });
$('btn-initial').addEventListener('click', () => { applyFen(START_FEN); render(); });
$('btn-apply-fen').addEventListener('click', () => {
  if (applyFen($('e-fen').value)) { msgEl.textContent = ''; render(); }
  else msgEl.textContent = 'Invalid FEN';
});
for (const id of ['e-turn', 'e-cK', 'e-cQ', 'e-ck', 'e-cq', 'e-ep', 'e-half', 'e-full'])
  $(id).addEventListener('input', () => { $('e-fen').value = buildFen(); });
$('btn-save').addEventListener('click', saveCurrent);
$('btn-refresh').addEventListener('click', refreshSaves);

$('btn-advanced').addEventListener('click', () => {
  advancedOpen = !advancedOpen;
  $('advanced-options').hidden = !advancedOpen;
  $('btn-advanced').textContent = advancedOpen ? 'Hide advanced options' : 'Show advanced options';
});
$('btn-apply-advanced').addEventListener('click', applyAdvanced);

applyFen(START_FEN);   // show something until the engine connects and sends the real position
render();