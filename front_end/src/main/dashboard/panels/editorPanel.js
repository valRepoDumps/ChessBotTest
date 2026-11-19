// Position editor panel. Owns the selected editing tool; edits the shared board/flags in place.
import { START_FEN, EMPTY_FEN, parseFen, buildFen, validateSetup } from '../../common/fenUtils.js';

const EDIT_HINT = 'Pick a piece, then click squares. Click an occupied square with the same piece to remove it.';

export function createEditorPanel(ctx) {
  const { state, backend, push, runBusy, applyFen, clearSelection } = ctx;
  const P = { tool: null };   // piece code, 'erase', or null

  const isActive = () => state.panel === 'editor';

  // ----- panel lifecycle (called by the game) -----
  function onOpen() {
    clearSelection();
    applyFen(state.currentFen);
    P.tool = null;
    state.msg = EDIT_HINT;
  }

  function onClose({ restore }) {
    if (restore) { applyFen(state.currentFen); state.msg = ''; }
  }

  // ----- board interaction while editing -----
  function onSquareClick(sq) {
    if (P.tool === 'erase' || state.board[sq] === P.tool) delete state.board[sq];
    else if (P.tool) state.board[sq] = P.tool;
    push();
  }

  async function startGame() {
    const err = validateSetup(state.board, state.flags);
    if (err) { state.msg = err; return push(); }
    await runBusy('Starting new game…', () => backend.newGame({ fen: buildFen(state.board, state.flags) }));
  }

  const view = () => ({
    tool: P.tool,
    fenText: isActive() ? buildFen(state.board, state.flags) : null,
  });

  const handlers = {
    'editor-open': () => ctx.openPanel('editor'),
    'editor-cancel': () => ctx.closePanel(true),
    'editor-start': startGame,
    'editor-clear': () => { applyFen(EMPTY_FEN); push(); },
    'editor-initial': () => { applyFen(START_FEN); push(); },
    'editor-apply-fen': ({ fen }) => { state.msg = applyFen(fen) ? '' : 'Invalid FEN'; push(); },
    'editor-flags': ({ flags }) => { state.flags = flags; push(); },
    'editor-tool': ({ tool }) => { P.tool = P.tool === tool ? null : tool; push(); },
  };

  return { isActive, onOpen, onClose, onSquareClick, view, handlers };
}