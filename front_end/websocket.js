// websocket.js
import { WebSocketServer } from 'ws';
import { state, send } from './state.js';

export function setupWebSocket() {
  const wss = new WebSocketServer({ port: state.PORT, host: '127.0.0.1' });

  wss.on('connection', (ws) => {
    state.client = ws;
    send('connection', true);

    ws.on('message', (raw) => {
      let msg;
      try { msg = JSON.parse(raw.toString()); } catch { return; }

      const p = msg.id && state.pending.get(msg.id);
      if (p) {
        clearTimeout(p.timer);
        state.pending.delete(msg.id);
        p.resolve(msg);
        return;
      }
    });

    ws.on('close', () => {
      if (state.client === ws) {
        state.client = null;
        send('connection', false);
      }
    });
  });

  return wss;
}