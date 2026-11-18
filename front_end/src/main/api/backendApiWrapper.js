// Main process: the API wrapper used by game.js. Same requests as the old ipc.js handlers,
// but as plain functions instead of ipcMain.handle callbacks.

import { boardRequest } from './boardRequest.js';
import { savesRequest } from './savesRequest.js';
import { optionRequest } from './optionRequest.js';
import { listEngineOptions, listEngines } from './engineRequest.js';
import { getValidMovesRequest } from './movesRequest.js';

export const backend = {
  // ---- saves ----
  listSaves: () =>
    savesRequest('list_saves', {}, { failPrefix: 'Listing saves failed' }),

  saveGame: (d) =>
    savesRequest('save_game', d,
      { okNotice: `Saved "${d?.name}"`, failPrefix: 'Save failed', extra: { saved: true } }),  // `saved` lets the UI clear the name box

  deleteSave: (d) =>
    savesRequest('delete_save', d, { okNotice: 'Save deleted', failPrefix: 'Delete failed' }),

  // ---- board ----
  getValidMoves: (square) => getValidMovesRequest('get_valid_moves', square),

  requestMove: (m) =>
    boardRequest('move_request', m, { failPrefix: 'Move failed' }),

  requestComputerMove: (d) =>
    boardRequest('computer_move', d, { failPrefix: 'Computer move failed', timeoutMs: 60000 }),

  undoTurn: (d) =>
    boardRequest('undo_turn', d, { failPrefix: 'Undo failed' }),

  newGame: (s) =>
    boardRequest('new_game', s, { okNotice: 'New game started', failPrefix: 'Could not start game' }),

  loadGame: (d) =>
    boardRequest('load_game', d, { failPrefix: 'Load failed' }),

  // ---- engines ----
  listEngines: () => listEngines('list_engines'),

  useEngine: (d) =>
    boardRequest('use_engine', d,
      { okNotice: 'Engine changed', failPrefix: 'Could not use engine', timeoutMs: 30000 }),

  listCurrentEngineOptions: (d) => listEngineOptions('list_engine_options', d),

  setEngineOption: (d) => optionRequest('set_engine_option', d),
  setEnginePreset: (d) => optionRequest('set_engine_preset', d),
};