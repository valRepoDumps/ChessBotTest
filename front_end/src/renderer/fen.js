import { FILES, S, $ } from './core.js';

export function parseFen(fen) {
  if (typeof fen !== 'string') return null;
  const [placement, turn = 'w', castling = '-', ep = '-', half = '0', full = '1'] = fen.trim().split(/\s+/);
  const rows = (placement || '').split('/');
  if (rows.length !== 8) return null;
  const board = {};
  for (let i = 0; i < 8; i++) {
    let f = 0;
    for (const ch of rows[i]) {
      if (/[1-8]/.test(ch)) f += Number(ch);
      else if (/[pnbrqkPNBRQK]/.test(ch) && f < 8) {
        board[FILES[f] + (8 - i)] = (ch === ch.toUpperCase() ? 'w' : 'b') + ch.toUpperCase();
        f++;
      } else return null;
    }
    if (f !== 8) return null;
  }
  return { board, turn, castling, ep, half: Number(half) || 0, full: Number(full) || 1 };
}

function placementFen() {
  const rows = [];
  for (let r = 8; r >= 1; r--) {
    let row = '', empty = 0;
    for (let f = 0; f < 8; f++) {
      const p = S.board[FILES[f] + r];
      if (!p) { empty++; continue; }
      if (empty) { row += empty; empty = 0; }
      row += p[0] === 'w' ? p[1] : p[1].toLowerCase();
    }
    rows.push(row + (empty || ''));
  }
  return rows.join('/');
}

function readFlags() {
  return {
    turn: $('e-turn').value,
    castling: ['K', 'Q', 'k', 'q'].filter((c) => $('e-c' + c).checked).join('') || '-',
    ep: $('e-ep').value.trim() || '-',
    half: Number($('e-half').value) || 0,
    full: Number($('e-full').value) || 1,
  };
}

export function buildFen() {
  const f = readFlags();
  return `${placementFen()} ${f.turn} ${f.castling} ${f.ep} ${f.half} ${f.full}`;
}

// Sets the board AND the editor's flag inputs from a FEN.
export function applyFen(fen) {
  const p = parseFen(fen);
  if (!p) return false;
  for (const sq in S.board) delete S.board[sq];
  Object.assign(S.board, p.board);
  $('e-turn').value = p.turn === 'b' ? 'b' : 'w';
  for (const c of ['K', 'Q', 'k', 'q']) $('e-c' + c).checked = p.castling.includes(c);
  $('e-ep').value = p.ep;
  $('e-half').value = p.half;
  $('e-full').value = p.full;
  return true;
}

// Returns an error string, or null if the editor position is playable.
export function validateSetup() {
  const pieces = Object.entries(S.board);
  for (const k of ['wK', 'bK']) {
    if (pieces.filter(([, c]) => c === k).length !== 1) return `Each side needs exactly one king (${k} count is wrong)`;
  }
  if (pieces.some(([sq, c]) => c[1] === 'P' && (sq[1] === '1' || sq[1] === '8'))) return 'Pawns cannot sit on rank 1 or 8';
  const { ep } = readFlags();
  if (ep !== '-' && !/^[a-h][36]$/.test(ep)) return 'En passant must be "-" or a square on rank 3 or 6';
  return null;
}