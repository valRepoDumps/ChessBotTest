// Core game state and the small helpers that only touch that state.
import { START_FEN, parseFen, flagsFromParsed } from '../../common/fenUtils.js';

export function createGameState() {
  const S = {
    board: {},              // square -> piece code, e.g. 'wP'
    highlights: {},         // square -> css color
    selected: null,
    validDestinations: [],
    selectToken: 0,         // invalidates in-flight getValidMoves calls
    busy: false,
    currentFen: START_FEN,  // last position confirmed by the engine
    gameOver: false,
    computerSide: false,    // false | 'w' | 'b'
    panel: null,            // 'editor' | 'saves' | 'vs-computer' | null
    msg: '',
    connected: false,
    flags: flagsFromParsed(parseFen(START_FEN)),
    promotion: null,        // { from, to, color } while the picker is open
  };

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

  applyFen(START_FEN);
  return { S, applyFen, clearSelection };
}

// Runs fn while showing `label`; ignores the call if something else is already running.
export function createRunBusy(S, push) {
  return async function runBusy(label, fn) {
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
  };
}