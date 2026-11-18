// Constants, shared state and tiny DOM helpers used by every renderer module.

export const FILES = 'abcdefgh';
export const GLYPH = { K: '♚', Q: '♛', R: '♜', B: '♝', N: '♞', P: '♟' };
export const COLORS = { green: '#4caf50', red: '#e5484d', blue: '#3c7fd6' };
export const START_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';
export const EMPTY_FEN = '8/8/8/8/8/8/8/8 w - - 0 1';
export const NO_PROMOTION = null;   // sent to the backend when a move is not a promotion

export const $ = (id) => document.getElementById(id);
export const setMsg = (text) => { $('msg').textContent = text; };
export const glyph = (type) => GLYPH[type] + '\uFE0E';

// The one shared mutable state object. Modules read and write it directly.
export const S = {
  board: {},              // square -> piece code, e.g. 'wP'
  highlights: new Map(),  // square -> css color
  selected: null,
  validDestinations: [],
  selectToken: 0,         // invalidates in-flight getValidMoves calls
  busy: false,
  currentFen: START_FEN,  // last position confirmed by the engine
  gameOver: false,
  mode: 'play',           // 'play' | 'edit'
  tool: null,             // piece code, 'erase', or null
  computerSide: false,    // false = manual | 'w' | 'b' = side the computer plays
};

// el('div', { className: 'x', onclick: fn }, child1, child2) -> element
export function el(tag, props = {}, ...kids) {
  const node = Object.assign(document.createElement(tag), props);
  node.append(...kids);
  return node;
}

export function note(ul, text) {
  ul.append(el('li', { className: 'empty', textContent: text }));
}

export function showPanel(which) {      // 'editor' | 'saves' | 'vs-computer' | null
  for (const id of ['editor', 'saves', 'vs-computer']) $(id).hidden = id !== which;
  document.body.classList.toggle('panel-open', !!which);
}