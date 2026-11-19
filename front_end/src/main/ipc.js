// Main process: the only IPC entry point left. Call initIpc(win) once the window exists.
 
import { ipcMain } from 'electron';
import { createGame } from './dashboard/game/game.js';
import { backend } from './api/backendApiWrapper.js';
 
export function initIpc(win) {
  const game = createGame(backend, (channel, data) => win.webContents.send(channel, data));
  ipcMain.handle('ui', (_e, intent) => game.handle(intent));
  return game;   // route backend pushes into it: onUpdateBoard / onNotice / onSaves / onConnection
}
