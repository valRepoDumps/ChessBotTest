// Main process: the game itself. Selection, moves, promotion, undo and the computer's turn.
// Everything else is its own module:
//   gameState.js     - shared state + runBusy
//   view.js          - builds and pushes the view the renderer draws
//   panelManager.js  - open/close/toggle of panels
//   backendEvents.js - board updates, notices and connection changes from the backend
//   editorPanel.js, savesPanel.js, computerPanel.js - the panels
//
// createGame(backend, send)
//   backend: async functions that talk to the external chess application
//   send:    (channel, data) => webContents.send(channel, data)

import { COLORS, NO_PROMOTION } from '../../common/constants.js';
import { createView } from '../../common/view.js';
import { createBackendEvents } from '../../common/backendEvents.js';

import { createPanelManager } from '../panels/panelManager.js';
import { createEditorPanel } from '../panels/editorPanel.js';
import { createSavesPanel } from '../panels/savesPanel.js';
import { createComputerPanel } from '../panels/computerPanel.js';

import { createGameState, createRunBusy } from './gameState.js';

export function createGame(backend, send) {
  const { S, applyFen, clearSelection } = createGameState();
  const panels = {};
  const { push } = createView(S, send, panels);
  const runBusy = createRunBusy(S, push);
  const panelManager = createPanelManager(S, panels, push);

  // What the game offers to its panels and event handlers.
  const ctx = {
    state: S, backend,
    push, runBusy, applyFen, clearSelection, maybeComputerMove,
    ...panelManager,
  };

  const editor = createEditorPanel(ctx);
  const saves = createSavesPanel(ctx);
  const computer = createComputerPanel(ctx);
  Object.assign(panels, { editor, saves, 'vs-computer': computer });

  const events = createBackendEvents(ctx, editor);

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
    if (editor.isActive()) return editor.onSquareClick(sq);
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

  // ---------- intent dispatch ----------
  const handlers = {
    ready: () => push(),
    square: onSquareClick,
    dragstart: () => { clearSelection(); },   // no push: re-rendering mid-drag cancels the drag
    drop: ({ from, to }) => (from && !editor.isActive() && !S.busy ? tryMove(from, to) : undefined),
    promotion: onPromotion,
    undo: () => runBusy('Undoing turn(s)',
      () => backend.undoTurn({ turns: String(S.computerSide ? 2 : 1) })),

    // panels register their own intents
    ...editor.handlers,
    ...saves.handlers,
    ...computer.handlers,
  };

  async function handle(intent) {
    const fn = handlers[intent?.type];
    return fn ? fn(intent) : undefined;
  }

  return { handle, ...events, onSaves: saves.onSaves };
}