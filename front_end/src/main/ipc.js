// ipc.js
import { ipcMain } from 'electron';
import { request } from './state.js';
import { boardRequest } from './api/boardRequest.js';
import { savesRequest } from './api/savesRequest.js';
import { optionRequest } from './api/optionRequest.js';
import { listEngineOptions, listEngines } from './api/engineRequest.js';
import { getValidMovesRequest } from './api/mistRequest.js';

ipcMain.handle('list-saves', () =>
  savesRequest('list_saves', {}, { failPrefix: 'Listing saves failed' }));

ipcMain.handle('save-game',   (_e, d) => savesRequest('save_game', d,
  { okNotice: `Saved "${d?.name}"`, failPrefix: 'Save failed', extra: { saved: true } }));  // `saved` lets the UI clear the name box

ipcMain.handle('delete-save', (_e, d) => savesRequest('delete_save', d,
  { okNotice: 'Save deleted', failPrefix: 'Delete failed' }));
//#endregion


ipcMain.handle('get-valid-moves', async (_e, square) => getValidMovesRequest('get_valid_moves', square));

ipcMain.handle('request-move', 
  (_e, m) => boardRequest('move_request', m,  
    { failPrefix: 'Move failed' }));

ipcMain.handle('request-computer-move',
  (_e, d) => boardRequest('computer_move', d,
    { failPrefix: 'Computer move failed', timeoutMs: 60000 }));

ipcMain.handle('undo-turn',    
  (_e, data) => boardRequest('undo_turn', data,    
    { failPrefix: 'Undo failed' }));

ipcMain.handle('new-game',     
  (_e, s) => boardRequest('new_game', s,      
    { 
      okNotice: 'New game started', 
      failPrefix: 'Could not start game' 
    }));

ipcMain.handle('load-game',    
  (_e, d) => boardRequest('load_game', d,     
    { failPrefix: 'Load failed' }));

//#endregion

//#region engines
ipcMain.handle('list-engines', async () => listEngines('list_engines'));

ipcMain.handle('use-engine', (_e, d) => boardRequest('use_engine', d,
    { okNotice: 'Engine changed', failPrefix: 'Could not use engine', timeoutMs: 30000 }));

ipcMain.handle('list-engine-options', async (_e, d) => listEngineOptions('list_engine_options', d));

ipcMain.handle('set-engine-option', (_e, d) => optionRequest('set_engine_option', d));
ipcMain.handle('set-engine-preset', (_e, d) => optionRequest('set_engine_preset', d));

//#endregion