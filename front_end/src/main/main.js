//main.js
import { app, BrowserWindow } from 'electron';
import path from 'path';
import { fileURLToPath } from 'url';
import { send, state } from './state.js';
import { setupWebSocket } from './websocket.js';
import './ipc.js';                      // registers all ipcMain handlers

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
      contextIsolation: false,
      nodeIntegration: false, //allow usage of import in renderer. 
      //MAKE SURE APP NEVER RENDER ANY OUTSIDE STUFF!! (NO LINK ONLINE, OR OUTSIDE THE APP)
      //MAKE SURE USER NEVER PROVIDE STUFF RENDERER RUNS. 
    },
  });
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