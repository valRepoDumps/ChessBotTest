// Thin view: draws the state pushed by main and forwards user intents.
// Load as a classic script (<script src="renderer.js"></script>), not type="module".

const FILES = 'abcdefgh';
const GLYPH = { K: '♚', Q: '♛', R: '♜', B: '♝', N: '♞', P: '♟' };
const PIECES = ['wK', 'wQ', 'wR', 'wB', 'wN', 'wP', 'bK', 'bQ', 'bR', 'bB', 'bN', 'bP'];

const $ = (id) => document.getElementById(id);
const glyph = (t) => GLYPH[t] + '\uFE0E';
const send = (type, data = {}) => window.chess.ui({ type, ...data });
const setVal = (id, v) => { if ($(id).value !== String(v)) $(id).value = v; };

function el(tag, props = {}, ...kids) {
  const node = Object.assign(document.createElement(tag), props);
  node.append(...kids);
  return node;
}
const note = (ul, text) => ul.append(el('li', { className: 'empty', textContent: text }));

let V = null;                 // last view
let lastOptionsRev = -1;
let lastSaveNameRev = 0;

// ================= Rendering =================
function renderBoard(v) {
  const boardEl = $('board');
  boardEl.classList.toggle('busy', v.busy);
  const squares = [];
  for (let r = 8; r >= 1; r--) {
    for (let f = 0; f < 8; f++) {
      const name = FILES[f] + r;
      const sq = el('div', {
        className: `sq ${(f + r) % 2 ? 'dark' : 'light'}${name === v.selected ? ' selected' : ''}`,
      });
      sq.dataset.sq = name;
      if (v.highlights[name]) {
        const hl = el('div', { className: 'hl' });
        hl.style.background = v.highlights[name];
        sq.append(hl);
      }
      if (f === 0) sq.append(el('span', { className: 'coord rank', textContent: r }));
      if (r === 1) sq.append(el('span', { className: 'coord file', textContent: FILES[f] }));
      const code = v.board[name];
      if (code) {
        sq.append(el('div', {
          className: 'piece ' + code[0],
          textContent: glyph(code[1]),
          draggable: !v.busy && v.mode === 'play',
        }));
      }
      squares.push(sq);
    }
  }
  boardEl.replaceChildren(...squares);
}

function renderPromotion(v) {
  document.querySelector('.promo-overlay')?.remove();
  if (!v.promotion) return;
  const overlay = el('div', { className: 'promo-overlay' });
  overlay.append(el('div', { className: 'promo-box' },
    ...['Q', 'R', 'B', 'N'].map((t) =>
      el('button', { className: v.promotion.color, textContent: glyph(t), onclick: () => send('promotion', { piece: t }) }))));
  overlay.onclick = (e) => { if (e.target === overlay) send('promotion', { piece: null }); };
  document.body.append(overlay);
}

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
      el('button', { textContent: 'Load', onclick: () => send('load', { save_id: s.save_id }) }),
      el('button', { textContent: 'Delete', onclick: () => send('delete-save', { save_id: s.save_id }) })));
  }
}

function renderEngines({ ok, reason, list, loading }) {
  const ul = $('engine-list');
  ul.replaceChildren();
  if (loading) return note(ul, 'Loading…');
  if (!ok) return note(ul, `Could not load engines: ${reason}`);
  if (!list.length) return note(ul, 'No engines found.');
  list.forEach((e, i) => ul.append(el('li', {},
    el('div', {}, el('span', { textContent: e.name }), el('small', { textContent: e.author })),
    el('button', { textContent: e.inUse ? 'In use' : 'Use', disabled: e.inUse, onclick: () => send('use-engine', { index: i }) }))));
}

// check -> checkbox, spin -> number within min/max, combo -> select, string -> text
function makeInput(p) {
  if (p.type === 'check') {
    const input = el('input', { type: 'checkbox', checked: p.value === 'true' });
    return { input, read: () => String(input.checked) };
  }
  let input;
  if (p.type === 'spin') {
    input = el('input', { type: 'number', value: p.value ?? '' });
    if (p.min != null) input.min = p.min;
    if (p.max != null) input.max = p.max;
  } else if (p.type === 'combo') {
    input = el('select');
    for (const v of p.vars || []) input.add(new Option(v, v));
    input.value = p.value ?? '';
  } else {
    input = el('input', { value: p.value === '<empty>' ? '' : (p.value ?? '') });
  }
  return { input, read: () => input.value };
}

const field = (p, input) => el('div', { className: 'row' }, el('label', {}, p.name, input));

function renderOption(p) {
  if (p.type === 'button') {
    return el('div', { className: 'row' },
      el('button', { textContent: p.name, onclick: () => send('send-option', { name: p.name }) }));
  }
  const { input, read } = makeInput(p);
  input.addEventListener('change', () => send('option-changed', { name: p.name, value: read() }));
  return field(p, input);
}

function renderPreset(preset) {
  const reads = [];
  const box = el('div', {}, el('div', { className: 'section-title', textContent: preset.label }));
  for (const p of preset.properties) {
    const { input, read } = makeInput(p);
    reads.push(read);
    box.append(field(p, input));
  }
  box.append(el('button', {
    textContent: 'Apply',
    onclick: () => send('apply-preset', { preset: preset.label, values: reads.map((r) => r()) }),
  }));
  return box;
}

function renderOptions(o, advancedOpen) {
  $('engine-options').hidden = !o.visible;
  $('btn-advanced').hidden = !o.advanced.length;
  $('btn-advanced').textContent = advancedOpen ? 'Hide advanced options' : 'Show advanced options';
  $('advanced-options').hidden = !advancedOpen;

  if (o.rev === lastOptionsRev) return;   // keep unsent control values until the options change
  lastOptionsRev = o.rev;
  $('preset-options').replaceChildren(
    el('div', { className: 'section-title', textContent: 'Presets' }),
    ...(o.presets.length
      ? o.presets.map(renderPreset)
      : [el('div', { className: 'empty', textContent: 'No presets available for this engine.' })]));
  $('advanced-list').replaceChildren(...o.advanced.map(renderOption));
}

function render(v) {
  V = v;
  renderBoard(v);
  renderPromotion(v);

  $('msg').textContent = v.msg;
  $('dot').classList.toggle('on', v.connected);
  $('conn').textContent = v.connected ? 'External application connected' : 'Waiting for external application';
  $('btn-undo').disabled = v.busy || v.mode !== 'play';

  for (const id of ['editor', 'saves', 'vs-computer']) $(id).hidden = id !== v.panel;
  document.body.classList.toggle('panel-open', !!v.panel);

  // editor
  for (const b of $('palette').children) b.classList.toggle('active', b.dataset.tool === v.tool);
  if (v.fenText != null) setVal('e-fen', v.fenText);
  setVal('e-turn', v.flags.turn);
  for (const c of ['K', 'Q', 'k', 'q']) $('e-c' + c).checked = v.flags[c];
  setVal('e-ep', v.flags.ep);
  setVal('e-half', v.flags.half);
  setVal('e-full', v.flags.full);

  // saves
  renderSaves(v.saves);
  if (v.saveNameRev !== lastSaveNameRev) { lastSaveNameRev = v.saveNameRev; $('save-name').value = ''; }

  // vs computer
  $('btn-computer').classList.toggle('active', !!v.computerSide);
  $('btn-computer').textContent =
    'vs Computer: ' + ({ w: 'plays White', b: 'plays Black' }[v.computerSide] ?? 'Off');
  $('vc-options').disabled = !v.computerSide;
  renderEngines(v.engines);
  renderOptions(v.options, v.advancedOpen);
}

// ================= Input -> intents =================
const readFlags = () => ({
  turn: $('e-turn').value,
  K: $('e-cK').checked, Q: $('e-cQ').checked, k: $('e-ck').checked, q: $('e-cq').checked,
  ep: $('e-ep').value, half: $('e-half').value, full: $('e-full').value,
});

// palette (built once)
for (const code of PIECES) {
  const b = el('button', { className: code[0], title: code, textContent: glyph(code[1]) });
  b.dataset.tool = code;
  $('palette').append(b);
}
const erase = el('button', { textContent: 'Erase' });
erase.dataset.tool = 'erase';
erase.style.cssText = 'width:auto;font-size:13px;padding:0 10px';
$('palette').append(erase);
$('palette').addEventListener('click', (e) => {
  const tool = e.target.closest('button')?.dataset.tool;
  if (tool) send('editor-tool', { tool });
});

const click = (id, fn) => $(id).addEventListener('click', fn);
click('btn-undo', () => send('undo'));
click('btn-new', () => send('editor-open'));
click('btn-cancel', () => send('editor-cancel'));
click('btn-start', () => send('editor-start'));
click('btn-clear', () => send('editor-clear'));
click('btn-initial', () => send('editor-initial'));
click('btn-apply-fen', () => send('editor-apply-fen', { fen: $('e-fen').value }));
for (const id of ['e-turn', 'e-cK', 'e-cQ', 'e-ck', 'e-cq', 'e-ep', 'e-half', 'e-full']) {
  $(id).addEventListener('input', () => send('editor-flags', { flags: readFlags() }));
}

click('btn-saves', () => send('toggle-saves'));
click('btn-refresh', () => send('refresh-saves'));
click('btn-save', () => send('save', { name: $('save-name').value.trim() }));

click('btn-computer', () => send('toggle-computer'));
const sendComputer = () => send('computer', { on: $('vc-on').checked, side: $('vc-side').value });
$('vc-on').addEventListener('change', sendComputer);
$('vc-side').addEventListener('change', sendComputer);
click('btn-refresh-engines', () => send('refresh-engines'));
click('btn-advanced', () => send('toggle-advanced'));
click('btn-apply-advanced', () => send('apply-advanced'));

document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape' && V?.promotion) send('promotion', { piece: null });
});

// board (delegated)
const boardEl = $('board');
boardEl.addEventListener('click', (e) => {
  const sq = e.target.closest('.sq');
  if (sq) send('square', { sq: sq.dataset.sq });
});
boardEl.addEventListener('dragstart', (e) => {
  const sq = e.target.closest('.sq');
  if (!sq || !V || V.mode !== 'play' || V.busy) return e.preventDefault();
  e.dataTransfer.setData('text/plain', sq.dataset.sq);
  send('dragstart');
});
boardEl.addEventListener('dragover', (e) => e.preventDefault());
boardEl.addEventListener('drop', (e) => {
  e.preventDefault();
  const sq = e.target.closest('.sq');
  const from = e.dataTransfer.getData('text/plain');
  if (sq && from) send('drop', { from, to: sq.dataset.sq });
});

// ================= Start =================
window.chess.onView(render);
send('ready');