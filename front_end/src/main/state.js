// state.js
import crypto from 'crypto';

export const state = {
  win: null,
  client: null,
  pending: new Map(),
  PORT: 8765,
  CONFIRM_TIMEOUT_MS: 10000
};

// Handlers for messages that must reach the game logic before the renderer.
// main.js fills these in once the window and game exist.
export const hooks = {};

// Core send function accessible anywhere
export function send(channel, payload) {
  const hook = hooks[channel];
  if (hook) return hook(payload);   // game updates its state, then pushes a 'view' to the renderer
  if (state.win && !state.win.isDestroyed()) {
    state.win.webContents.send(channel, payload);
  }
}

// Core request helper accessible anywhere
export function request(type, payload = {}, timeoutMs = state.CONFIRM_TIMEOUT_MS) {
  if (!state.client || state.client.readyState !== 1) {
    return Promise.resolve({ ok: false, reason: 'No external application connected' });
  }

  const id = crypto.randomUUID();

  return new Promise((resolve) => {
    const timer = setTimeout(() => {
      state.pending.delete(id);
      resolve({ ok: false, reason: `Timed out after ${timeoutMs} ms` });
    }, timeoutMs);
    state.pending.set(id, { resolve, timer });
    state.client.send(JSON.stringify({ type, id, ...payload }));
  });
}