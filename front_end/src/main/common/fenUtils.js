// Main process: pure FEN helpers (no DOM).

export const FILES = 'abcdefgh';
export const START_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';
export const EMPTY_FEN = '8/8/8/8/8/8/8/8 w - - 0 1';

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

// Editor flag fields, kept as the raw values the inputs hold.
export function flagsFromParsed(p) {
  return {
    turn: p.turn === 'b' ? 'b' : 'w',
    K: p.castling.includes('K'), Q: p.castling.includes('Q'),
    k: p.castling.includes('k'), q: p.castling.includes('q'),
    ep: p.ep, half: p.half, full: p.full,
  };
}

export function placementFen(board) {
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

export function buildFen(board, f) {
  const castling = ['K', 'Q', 'k', 'q'].filter((c) => f[c]).join('') || '-';
  const ep = String(f.ep ?? '').trim() || '-';
  return `${placementFen(board)} ${f.turn} ${castling} ${ep} ${Number(f.half) || 0} ${Number(f.full) || 1}`;
}

// Returns an error string, or null if the editor position is playable.
export function validateSetup(board, f) {
  const pieces = Object.entries(board);
  for (const k of ['wK', 'bK']) {
    if (pieces.filter(([, c]) => c === k).length !== 1) return `Each side needs exactly one king (${k} count is wrong)`;
  }
  if (pieces.some(([sq, c]) => c[1] === 'P' && (sq[1] === '1' || sq[1] === '8'))) return 'Pawns cannot sit on rank 1 or 8';
  const ep = String(f.ep ?? '').trim() || '-';
  if (ep !== '-' && !/^[a-h][36]$/.test(ep)) return 'En passant must be "-" or a square on rank 3 or 6';
  return null;
}