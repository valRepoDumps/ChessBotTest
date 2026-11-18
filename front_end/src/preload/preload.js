const { contextBridge, ipcRenderer } = require('electron');

// The renderer only sends intents and receives views; all logic lives in the main process.
contextBridge.exposeInMainWorld('chess', {
  ui: (intent) => ipcRenderer.invoke('ui', intent),
  onView: (cb) => ipcRenderer.on('view', (_e, view) => cb(view)),
});