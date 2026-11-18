import { app, BrowserWindow, ipcMain } from 'electron';
import path from 'path';
import crypto from 'crypto';
import { WebSocketServer } from 'ws';
import { fileURLToPath } from 'url';

import {boardRequest} from './api/boardRequest.js';
import {request, send} from './api/base_request.js'

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = 8765;
const CONFIRM_TIMEOUT_MS = Number(process.env.CHESS_TIMEOUT_MS) || 10000;

let win = null;
let client = null;              // the connected external application
const pending = new Map();      // request id -> { resolve, timer }

function createWindow() {
  win = new BrowserWindow({
    width: 760,
    height: 900,
    backgroundColor: '#1d1b19',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
  });
  win.setMenuBarVisibility(false);
  win.loadFile('index.html');
  win.webContents.on('did-finish-load', () => send('connection', !!client));
  win.webContents.openDevTools()
  if (process.env.CHESS_DEVTOOLS) win.webContents.openDevTools(); // was `mainWindow` (undefined) -> crashed
}

// ---------- WebSocket server: the external application connects here ----------
const wss = new WebSocketServer({ port: PORT, host: '127.0.0.1' });

wss.on('connection', (ws) => {
  client = ws;
  send('connection', true);

  ws.on('message', (raw) => {
    let msg;
    try { msg = JSON.parse(raw.toString()); } catch { return; }

    const p = msg.id && pending.get(msg.id);
    if (p){
      clearTimeout(p.timer);
      pending.delete(msg.id);
      p.resolve(msg);
      return;
    }

    // //should unsolicited push be included??? NO
    // if (msg.type === 'update_board') send('update_board', msg);
  });

  ws.on('close', () => {
    if (client === ws) {
      client = null;
      send('connection', false);
    }
  });
});

//#region Generic request/response helper
//#endregion

app.whenReady().then(createWindow);
app.on('window-all-closed', () => {
  wss.close();
  app.quit();
});