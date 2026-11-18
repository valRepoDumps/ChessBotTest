// Board editor panel, plus the shared panel toggle.

import { EMPTY_FEN, START_FEN, S, $, setMsg, el, glyph, showPanel } from './core.js';
import { applyFen, buildFen, validateSetup } from './fen.js';
import { render, clearSelection, runBusy } from './board.js';

const palette = $('palette');
const PIECES = ['wK', 'wQ', 'wR', 'wB', 'wN', 'wP', 'bK', 'bQ', 'bR', 'bB', 'bN', 'bP'];

const markTool = () => {
  for (const b of palette.children) b.classList.toggle('active', b.dataset.tool === S.tool);
};

function buildPalette() {
  for (const code of PIECES) {
    const b = el('button', { className: code[0], title: code, textContent: glyph(code[1]) });
    b.dataset.tool = code;
    palette.append(b);
  }
  const erase = el('button', { textContent: 'Erase' });
  erase.dataset.tool = 'erase';
  erase.style.cssText = 'width:auto;font-size:13px;padding:0 10px';
  palette.append(erase);

  palette.addEventListener('click', (e) => {
    const t = e.target.closest('button')?.dataset.tool;
    if (!t) return;
    S.tool = S.tool === t ? null : t;
    markTool();
  });
}

function openEditor() {
  clearSelection();
  applyFen(S.currentFen);          // sync board and flag inputs with the live game
  S.mode = 'edit';
  S.tool = null;
  markTool();
  showPanel('editor');
  setMsg('Pick a piece, then click squares. Click an occupied square with the same piece to remove it.');
  render();
}

// restore = true discards edits (board and flags) and clears the hint message.
export function closeEditor(restore) {
  if (restore) { applyFen(S.currentFen); setMsg(''); }
  S.mode = 'play';
  showPanel(null);
  render();
}

// Opens a side panel (closing the editor first) or closes it if already open.
export function togglePanel(name, onOpen) {
  if (S.mode === 'edit') closeEditor(true);
  if ($(name).hidden) { showPanel(name); onOpen(); }
  else showPanel(null);
}

async function startGame() {
  const err = validateSetup();
  if (err) return setMsg(err);
  await runBusy('Starting new game…', () => window.chess.newGame({ fen: buildFen() }));
}

// ================= Wiring =================
buildPalette();
$('btn-new').addEventListener('click', openEditor);
$('btn-cancel').addEventListener('click', () => closeEditor(true));
$('btn-start').addEventListener('click', startGame);
$('btn-clear').addEventListener('click', () => { applyFen(EMPTY_FEN); render(); });
$('btn-initial').addEventListener('click', () => { applyFen(START_FEN); render(); });
$('btn-apply-fen').addEventListener('click', () => {
  if (applyFen($('e-fen').value)) { setMsg(''); render(); }
  else setMsg('Invalid FEN');
});
for (const id of ['e-turn', 'e-cK', 'e-cQ', 'e-ck', 'e-cq', 'e-ep', 'e-half', 'e-full']) {
  $(id).addEventListener('input', () => { $('e-fen').value = buildFen(); });
}