const WebSocket = require('ws');
const URL = `ws://127.0.0.1:${process.env.CHESS_PORT || 8765}`;

const PIECE_INDICES = {
  'P': 0, 'N': 1, 'B': 2, 'R': 3, 'Q': 4, 'K': 5,
  'p': 6, 'n': 7, 'b': 8, 'r': 9, 'q': 10, 'k': 11
};
const FILES = 'abcdefgh';
const START_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR';

/**
 * Converts a FEN piece placement string into an array of 12 64-bit bitboards (as strings).
 * Bit 0 = a1, Bit 63 = h8.
 */
function fenToBitboards(fen) {
  const bitboards = new Array(12).fill(0n);
  const rows = fen.split('/');

  for (let r = 0; r < 8; r++) {
    const rank = 8 - r;
    let f = 0;
    for (const char of rows[r]) {
      if (!isNaN(char)) {
        f += parseInt(char, 10);
      } else {
        const pieceIdx = PIECE_INDICES[char];
        if (pieceIdx !== undefined) {
          const sqIndex = (rank - 1) * 8 + f;
          bitboards[pieceIdx] |= (1n << BigInt(sqIndex));
        }
        f++;
      }
    }
  }
  return bitboards.map(bb => bb.toString());
}

/**
 * Applies a move to a FEN placement string and returns the new FEN placement.
 */
function applyMoveToFen(fen, from, to) {
  const rows = fen.split('/');
  const boardMap = {};

  for (let r = 8; r >= 1; r--) {
    const rowIndex = 8 - r;
    let f = 0;
    for (const char of rows[rowIndex]) {
      if (!isNaN(char)) {
        f += parseInt(char, 10);
      } else {
        boardMap[FILES[f] + r] = char;
        f++;
      }
    }
  }

  if (boardMap[from]) {
    boardMap[to] = boardMap[from];
    delete boardMap[from];
  }

  let newRows = [];
  for (let r = 8; r >= 1; r--) {
    let row = '', empty = 0;
    for (let f = 0; f < 8; f++) {
      const sq = FILES[f] + r;
      const p = boardMap[sq];
      if (!p) { empty++; continue; }
      if (empty) { row += empty; empty = 0; }
      row += p;
    }
    newRows.push(row + (empty || ''));
  }
  return newRows.join('/');
}

let currentFen = START_FEN;

function connect() {
  const ws = new WebSocket(URL);

  ws.on('open', () => {
    console.log('connected to', URL);

    // Send initial board state on connection
    const bitboards = fenToBitboards(currentFen);
    ws.send(JSON.stringify({
      type: 'init_board',
      bitboards: bitboards
    }));
  });

  ws.on('error', (err) => console.log('connection error:', err.code || err.message));
  ws.on('close', () => {
    console.log('disconnected, retrying in 2s…');
    setTimeout(connect, 2000);
  });

  ws.on('message', (raw) => {
    const msg = JSON.parse(raw.toString());
    if (msg.type !== 'move_request') return;
    console.log('move request:', msg);

    // Apply the move using the current session FEN
    currentFen = applyMoveToFen(msg.placement || currentFen, msg.from, msg.to);
    const bitboards = fenToBitboards(currentFen);

    // Confirm the move and return the updated bitboards
    ws.send(JSON.stringify({
      type: 'move_response',
      id: msg.id,
      approved: true,
      bitboards: bitboards
    }));

    // Highlight the squares that were just played
    ws.send(JSON.stringify({ type: 'highlight', squares: [msg.from, msg.to], color: 'yellow' }));
  });
}

connect();