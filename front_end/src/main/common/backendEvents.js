// Messages pushed by the backend (board updates, notices, connection changes).
import { START_FEN, parseFen } from './fenUtils.js';
import { GAME_OVER_TEXT } from './constants.js';

export function createBackendEvents(ctx, editor) {
  const { state: S, backend, push, runBusy, applyFen, clearSelection, closePanel } = ctx;

  function onUpdateBoard(msg) {
    if (!parseFen(msg.fen)) { S.msg = 'Received an invalid board from the engine'; return push(); }

    S.currentFen = msg.fen;
    if (editor.isActive() && !msg.is_new_game) return;   // don't overwrite an in-progress edit
    applyFen(msg.fen);
    clearSelection();

    const over = msg.game_over;   // a missing field means the game is still running
    S.gameOver = !!over && over !== 'indeterminate';
    if (S.gameOver) S.msg = `Game over: ${GAME_OVER_TEXT[over] ?? over}`;

    if (msg.is_new_game) closePanel(false);
    else push();
  }

  function onNotice(text) { if (text) { S.msg = text; push(); } }

  // When the engine first connects (or reconnects after dropping), ask it for a fresh game.
  function onConnection(ok) {
    const was = S.connected;
    S.connected = ok;
    push();
    if (ok && !was) runBusy('Starting new game…', () => backend.newGame({ fen: START_FEN }));
  }

  return { onUpdateBoard, onNotice, onConnection };
}