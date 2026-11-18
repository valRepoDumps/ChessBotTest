const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('chess', {
  requestMove: (move) => ipcRenderer.invoke('request-move', move), 
//example: {
//   "type": "move_request", "id": "e4d1…",
//   "from": "e2", "to": "e4",
//   "promotion": null (or Q,R,B,K),
//   "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
// }
//expect: update_board
//reject: { "type": "failure", "id": "e4d1…", "ok": false, "reason": "King would be in check" }

  requestComputerMove: (data) => ipcRenderer.invoke('request-computer-move', data),
//example: { "type": "computer_move", "id": "…", "computer_side": "w" | "b" }
//expect: update_board
//reject: { "type": "failure", "id": "…", "ok": false, "reason": "Engine closed unexpectedly" }

  getValidMoves: (square) => ipcRenderer.invoke('get-valid-moves', square),
  //example: { "type": "get_valid_moves", "id": "9c02…", "square": "e2" }
  //expect: { "type": "get_valid_moves_response", "id": "9c02…", "moves": [ { "to": "e3" }, { "to": "e4" } ] }
 
  newGame: (setup) => ipcRenderer.invoke('new-game', setup),
//   example: {
//   "type": "new_game",
//   "id": "5b0c2e1e-6a1b-4f5e-9d3e-0c9f1a7e2b11",
//   "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
//   }
//expect: update_board
//reject: { "type": "failure", "id": "e4d1…", "ok": false, "reason": "King would be in check" }

  saveGame: (data) => ipcRenderer.invoke('save-game', data),
  //example: { "type": "save_game", "id": "a1f0…", "name": "Endgame practice" }
  //expect: {
//   "type": "save_game_response",
//   "id": "a1f0…",
//   "ok": true,
//   "save_id": "save_0007",
//   "name": "Endgame practice",
//   "saved_at": "2026-10-02T09:41:12Z"
// }
//reject: { "type": "failure", "id": "e4d1…", "ok": false, "reason": "disk full" }

  loadGame: (data) => ipcRenderer.invoke('load-game', data),
  //example: { 
  // "type": "load_game", 
  // "id": "77ab…", 
  // "save_id": "save_0007", 
  //expect: update_board

  listSaves: () => ipcRenderer.invoke('list-saves'),
  //example: { "type": "list_saves", "id": "c93d…" }
  //expect: {
  //   "type": "list_saves_response",
  //   "id": "c93d…",
  //   "ok": true,
  //   "saves": [
  //     { "save_id": "save_0007", "name": "Endgame practice", "saved_at": "2026-10-02T09:41:12Z"},
  //     { "save_id": "save_0006", "name": "Untitled",         "saved_at": "2026-10-01T18:02:55Z"}
  //   ]
  // }
  //reject: { "type": "failure", "id": "e4d1…", "ok": false, "reason": "rejecte forr some reason" }

  deleteSave: (data) => ipcRenderer.invoke('delete-save', data),
  //example: { "type": "delete_save", "id": "c93d…", "save_id": "save_0003" }
  //expect: {
    //expect: {
    //   "type": "list_saves_response",
    //   "id": "c93d…",
    //   "ok": true,
    //   "saves": [
    //     { "save_id": "save_0007", "name": "Endgame practice", "saved_at": "2026-10-02T09:41:12Z"},
    //     { "save_id": "save_0006", "name": "Untitled",         "saved_at": "2026-10-01T18:02:55Z"}
    //   ]
  // }
  undoTurn: (data) => ipcRenderer.invoke('undo-turn', data),
  //example: {"type": "undo_turn", "id": "adw3"}
  //expects: update_board
  //reject: //reject: { "type": "failure", "id": "e4d1…", "ok": false, "reason": "King would be in check" }

  onConnection: (cb) => ipcRenderer.on('connection', (_e, ok) => cb(ok)),

  //renderer handle these. 
  onNotice: (cb) => ipcRenderer.on('notice', (_e, text) => cb(text)),
  onSaves: (cb) => ipcRenderer.on('saves', (_e, msg) => cb(msg)),

  onUpdateBoard: (cb) => ipcRenderer.on('update_board', (_e, msg) => cb(msg)),
  //expect {type = "update_board", 
  // "id": "qeww3...", 
  // "ok": true, 
  // "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
  // "is_new_game": whether to reset boarrd}
  
  listEngines: () => ipcRenderer.invoke('list-engines'),
  //example: { "type": "list_engines", "id": "…" }
  //expect: { "type": "list_engines_response", "id": "…", "ok": true, "engines": [ { "name": "Stockfish 17", … } ] }
  //reject: { "type": "failure", "id": "…", "ok": false, "reason": "…" }

  useEngine: (data) => ipcRenderer.invoke('use-engine', data),
  //example: { "type": "use_engine", "id": "…", "engine": { …one entry exactly as listed… } }
  //expect: update_board
  //reject: { "type": "failure", "id": "…", "ok": false, "reason": "Engine did not start" }

  listCurrentEngineOptions: (data) => ipcRenderer.invoke('list-engine-options', data),
  setEngineOption: (data) => ipcRenderer.invoke('set-engine-option', data),
  setEnginePreset: (data) => ipcRenderer.invoke('set-engine-preset', data),
});