// Board rendering, square selection, drag and drop, move requests.

import { FILES, COLORS, NO_PROMOTION, S, $, setMsg, el, glyph } from './core.js';
import { buildFen } from './fen.js';

const boardEl = $('board');

// ================= Rendering =================
export function render() {
  if (S.mode === 'edit') $('e-fen').value = buildFen();
  boardEl.classList.toggle('busy', S.busy);
  $('btn-undo').disabled = S.busy || S.mode !== 'play';

  const squares = [];
  for (let r = 8; r >= 1; r--) {
    for (let f = 0; f < 8; f++) {
      const name = FILES[f] + r;
      const sq = el('div', {
        className: `sq ${(f + r) % 2 ? 'dark' : 'light'}${name === S.selected ? ' selected' : ''}`,
      });
      sq.dataset.sq = name;

      if (S.highlights.has(name)) {
        const hl = el('div', { className: 'hl' });
        hl.style.background = S.highlights.get(name);
        sq.append(hl);
      }
      if (f === 0) sq.append(el('span', { className: 'coord rank', textContent: r }));
      if (r === 1) sq.append(el('span', { className: 'coord file', textContent: FILES[f] }));

      const code = S.board[name];
      if (code) {
        sq.append(el('div', {
          className: 'piece ' + code[0],
          textContent: glyph(code[1]),
          draggable: !S.busy && S.mode === 'play',
        }));
      }
      squares.push(sq);
    }
  }
  boardEl.replaceChildren(...squares);
}

// ================= Busy wrapper =================
// Runs fn while locking the UI; ignored if another request is already in flight.
export async function runBusy(label, fn) {
  if (S.busy) return null;
  S.busy = true;
  if (label) setMsg(label);
  render();
  try { return await fn(); }
  finally {
    S.busy = false;
    if (label && $('msg').textContent === label) setMsg('');   // only clear our own label
    render();
  }
}

// ================= Promotion picker =================
function isPromotion(from, to) {
  const p = S.board[from];
  return p?.[1] === 'P' && to[1] === (p[0] === 'w' ? '8' : '1');
}

function pickPromotion(color) {
  return new Promise((resolve) => {
    const overlay = el('div', { className: 'promo-overlay' });
    const onKey = (e) => { if (e.key === 'Escape') done(null); };
    const done = (value) => {
      document.removeEventListener('keydown', onKey);
      overlay.remove();
      resolve(value);
    };
    overlay.append(el('div', { className: 'promo-box' },
      ...['Q', 'R', 'B', 'N'].map((t) =>
        el('button', { className: color, textContent: glyph(t), onclick: () => done(t) }))));
    overlay.onclick = (e) => { if (e.target === overlay) done(null); };
    document.addEventListener('keydown', onKey);
    document.body.append(overlay);
  });
}

// ================= Selection & moves =================
export function clearSelection() {
  S.selectToken++;
  S.selected = null;
  S.highlights.clear();
  S.validDestinations = [];
}

async function selectSquare(name) {
  clearSelection();
  const token = S.selectToken;
  S.selected = name;
  S.highlights.set(name, COLORS.blue);
  render();                                   // instant feedback

  let moves = [];
  try { moves = await window.chess.getValidMoves(name); }
  catch (e) { console.error(e); }

  if (token !== S.selectToken) return;        // user already clicked elsewhere
  S.validDestinations = Array.isArray(moves) ? moves : [];
  for (const m of S.validDestinations) {
    if (m.to) S.highlights.set(m.to, m.captured ? COLORS.red : COLORS.green);
  }
  render();
}

async function onSquareClick(name) {
  if (S.mode === 'edit') {
    if (S.tool === 'erase' || S.board[name] === S.tool) delete S.board[name];
    else if (S.tool) S.board[name] = S.tool;
    return render();
  }
  if (S.busy) return;

  if (S.selected === name) { clearSelection(); return render(); }

  if (S.selected && S.validDestinations.some((m) => m.to === name)) {
    const from = S.selected;
    clearSelection();
    return tryMove(from, name);
  }

  if (S.board[name]) return selectSquare(name);
  clearSelection();
  render();
}

// The new board and the notice are pushed by main.js; this only locks the UI while waiting.
async function tryMove(from, to) {
  if (from === to || !S.board[from]) { clearSelection(); return render(); }

  let promotion = NO_PROMOTION;
  if (isPromotion(from, to)) {
    promotion = await pickPromotion(S.board[from][0]);
    if (!promotion) return render();          // picker cancelled
  }

  const r = await runBusy(`Waiting for confirmation of ${from} → ${to}…`,
    () => window.chess.requestMove({ from, to, promotion, computer_side: S.computerSide }));

  if (r?.ok) await maybeComputerMove();
}

// Second half of a computer turn: the backend replies with another update_board.
// Does nothing if computer mode is off or the game is over.
export async function maybeComputerMove() {
  if (!S.computerSide || S.gameOver) return;
  await runBusy('Computer is thinking…',
    () => window.chess.requestComputerMove({ computer_side: S.computerSide }));
}

// ================= Delegated listeners (one set, not per square) =================
boardEl.addEventListener('click', (e) => {
  const sq = e.target.closest('.sq');
  if (sq) onSquareClick(sq.dataset.sq);
});

boardEl.addEventListener('dragstart', (e) => {
  const sq = e.target.closest('.sq');
  if (!sq || S.mode !== 'play' || S.busy) return e.preventDefault();
  e.dataTransfer.setData('text/plain', sq.dataset.sq);
  clearSelection();   // no render here: re-rendering mid-drag cancels the drag
});

boardEl.addEventListener('dragover', (e) => e.preventDefault());

boardEl.addEventListener('drop', (e) => {
  e.preventDefault();
  const sq = e.target.closest('.sq');
  const from = e.dataTransfer.getData('text/plain');
  if (sq && from && S.mode === 'play') tryMove(from, sq.dataset.sq);
});