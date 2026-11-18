// Entry point: loads the feature modules and handles messages pushed from main.

import { START_FEN, S, $, setMsg } from './core.js';
import { parseFen, applyFen } from './fen.js';
import { render, clearSelection, runBusy } from './board.js';
import { closeEditor } from './editor.js';
import './saves.js';
import './engines.js';

const GAME_OVER_TEXT = { draw: 'draw', w: 'white won', b: 'black won' };

window.chess.onUpdateBoard((msg) => {
  if (!parseFen(msg.fen)) return setMsg('Received an invalid board from the engine');

  S.currentFen = msg.fen;
  if (S.mode === 'edit' && !msg.is_new_game) return;   // don't overwrite an in-progress edit
  applyFen(msg.fen);
  clearSelection();

  // A missing game_over field means the game is still running.
  const over = msg.game_over;
  S.gameOver = !!over && over !== 'indeterminate';
  if (S.gameOver) setMsg(`Game over: ${GAME_OVER_TEXT[over] ?? over}`);

  if (msg.is_new_game) closeEditor(false);
  else render();
});

window.chess.onNotice((text) => { if (text) setMsg(text); });

// When the engine first connects (or reconnects after dropping), ask it for a fresh game.
let connected = false;
window.chess.onConnection((ok) => {
  $('dot').classList.toggle('on', ok);
  $('conn').textContent = ok ? 'External application connected' : 'Waiting for external application';
  if (ok && !connected) {
    runBusy('Starting new game…', () => window.chess.newGame({ fen: START_FEN }));
  }
  connected = ok;
});

$('btn-undo').addEventListener('click', () =>
  runBusy('Undoing turn(s)', () => window.chess.undoTurn({ turns: String(S.computerSide ? 2 : 1) })));

applyFen(START_FEN);   // show something until the engine connects
render();