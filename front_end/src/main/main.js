//main.js
import { app, BrowserWindow } from 'electron';
import path from 'path';
import { fileURLToPath } from 'url';
import { send, state, hooks } from './state.js';
import { setupWebSocket } from './websocket.js';
import { initIpc } from './ipc.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const preloadPath = path.join(__dirname, '../preload/preload.js');
const indexPath = path.join(__dirname, '../renderer/index.html');

function createWindow() {
  state.win = new BrowserWindow({
    width: 760,
    height: 900,
    backgroundColor: '#1d1b19',
    webPreferences: {
      preload: preloadPath,
      contextIsolation: true,
      nodeIntegration: false
    },
  });

  // The game needs the window, so it is created here, not at the top of the file.
  const game = initIpc(state.win);
  hooks.update_board = game.onUpdateBoard;
  hooks.notice = game.onNotice;
  hooks.saves = game.onSaves;
  hooks.connection = game.onConnection;

  state.win.setMenuBarVisibility(false);
  state.win.loadFile(indexPath);
  state.win.webContents.on('did-finish-load', () => send('connection', !!state.client));
  state.win.webContents.openDevTools();   // remove when done debugging
}

const wss = setupWebSocket();
wss.on('error', (err) => console.error('WebSocket server error:', err));

app.whenReady().then(createWindow);
app.on('window-all-closed', () => {
  wss.close();
  app.quit();
});