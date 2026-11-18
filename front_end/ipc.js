// ipc.js
import { ipcMain } from 'electron';
import { request } from './state.js';
import { boardRequest } from './api/boardRequest.js';

ipcMain.handle('list-saves',  () => savesRequest('list_saves', {failPrefix: 'Listing saves failed'}));

ipcMain.handle('save-game',   (_e, d) => savesRequest('save_game', d,
  { okNotice: `Saved "${d?.name}"`, failPrefix: 'Save failed', extra: { saved: true } }));  // `saved` lets the UI clear the name box

ipcMain.handle('delete-save', (_e, d) => savesRequest('delete_save', d,
  { okNotice: 'Save deleted', failPrefix: 'Delete failed' }));
//#endregion


ipcMain.handle('get-valid-moves', async (_e, square) => {
  const r = await request('get_valid_moves', { square }, 5000);
  return Array.isArray(r.moves) ? r.moves : [];   
});

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
ipcMain.handle('list-engines', async () => {
  const r = await request('list_engines', {}, 30000);   // the backend starts each candidate to check it
  const ok = r.ok !== false;
  return { ok, reason: ok ? '' : (r.reason || 'no response'),
          engines: ok && Array.isArray(r.engines) ? r.engines : [] };
  });

ipcMain.handle('use-engine', (_e, d) =>
  boardRequest('use_engine', d,
    { okNotice: 'Engine changed', failPrefix: 'Could not use engine', timeoutMs: 30000 }));

    ipcMain.handle('list-engine-options', async (_e, d) => {
  const r = await request('list_engine_options', { advanced: true, ...d });
  const ok = r.ok === true;   // "No engine selected" arrives here as a failure -> nothing is shown
  return {
    ok,
    reason: ok ? '' : (r.reason || 'no response'),
    presets: ok && Array.isArray(r.preset_options) ? r.preset_options : [],
    advanced: ok && Array.isArray(r.advanced_options) ? r.advanced_options : [],
  };
});

ipcMain.handle('set-engine-option', (_e, d) => optionRequest('set_engine_option', d));
ipcMain.handle('set-engine-preset', (_e, d) => optionRequest('set_engine_preset', d));

//#endregion