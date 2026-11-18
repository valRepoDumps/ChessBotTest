// Save / load panel.

import { S, $, el, note } from './core.js';
import { runBusy, maybeComputerMove } from './board.js';
import { togglePanel } from './editor.js';

function renderSaves({ ok, reason, saves = [], loading }) {
  const ul = $('save-list');
  ul.replaceChildren();
  if (loading) return note(ul, 'Loading…');
  if (!ok) return note(ul, `Could not load saves: ${reason}`);
  if (!saves.length) return note(ul, 'No saved games yet. Name this game and save it.');

  for (const s of saves) {
    ul.append(el('li', {},
      el('div', {},
        el('span', { textContent: s.name || s.save_id }),
        el('small', { textContent: s.saved_at ? new Date(s.saved_at).toLocaleString() : s.save_id })),
      el('button', { textContent: 'Load', onclick: () => loadGame(s.save_id) }),
      el('button', {
        textContent: 'Delete',
        onclick: () => runBusy('Deleting…', () => window.chess.deleteSave({ save_id: s.save_id })),
      })));
  }
}

// The list arrives asynchronously through the 'saves' push below.
function refreshSaves() {
  renderSaves({ ok: true, loading: true });
  window.chess.listSaves();
}

async function loadGame(save_id) {
  const r = await runBusy('Loading…',
    () => window.chess.loadGame({ save_id, computer_side: S.computerSide }));
  if (r?.ok) await maybeComputerMove();
}

window.chess.onSaves((msg) => {
  renderSaves(msg);
  if (msg.saved) $('save-name').value = '';
});

$('btn-saves').addEventListener('click', () => togglePanel('saves', refreshSaves));
$('btn-refresh').addEventListener('click', refreshSaves);
$('btn-save').addEventListener('click', () => {
  const name = $('save-name').value.trim() || 'Untitled';
  runBusy(null, () => window.chess.saveGame({ name }));
});
