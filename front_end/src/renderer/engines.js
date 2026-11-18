// "vs Computer" panel: computer side, engine list, engine options and presets.

import { S, $, setMsg, el, note } from './core.js';
import { runBusy, render, clearSelection, maybeComputerMove } from './board.js';
import { togglePanel } from './editor.js';

// ================= Computer side =================
function updateComputerButton() {
  $('btn-computer').classList.toggle('active', !!S.computerSide);
  $('btn-computer').textContent =
    'vs Computer: ' + ({ w: 'plays White', b: 'plays Black' }[S.computerSide] ?? 'Off');
}

function applyComputerSettings() {
  S.computerSide = $('vc-on').checked ? $('vc-side').value : false;
  $('vc-options').disabled = !S.computerSide;   // greys out side + engines while off
  clearSelection();
  updateComputerButton();
  render();
  maybeComputerMove();   // if it's already the computer's turn it plays now
}

// ================= Engine list =================
let engineResult = null;     // last list from the backend
let usedEngineKey = null;    // JSON of the engine the backend confirmed

async function refreshEngines() {
  renderEngines({ loading: true });
  engineResult = await window.chess.listEngines();
  if (!usedEngineKey && engineResult.ok) {
    const builtin = engineResult.engines.find((e) => e.builtin);   // built-in engine is the default
    if (builtin) usedEngineKey = JSON.stringify(builtin);
  }
  renderEngines(engineResult);
}

function renderEngines({ ok, reason, engines, loading }) {
  const ul = $('engine-list');
  ul.replaceChildren();
  if (loading) return note(ul, 'Loading…');
  if (!ok) return note(ul, `Could not load engines: ${reason}`);
  if (!engines.length) return note(ul, 'No engines found.');

  for (const e of engines) {
    const inUse = JSON.stringify(e) === usedEngineKey;
    ul.append(el('li', {},
      el('div', {},
        el('span', { textContent: e.name || e.path || 'Engine' }),
        el('small', { textContent: e.author || '' })),
      el('button', { textContent: inUse ? 'In use' : 'Use', disabled: inUse, onclick: () => useEngine(e) })));
  }
}

async function useEngine(engine) {
  const r = await runBusy('Switching engine…', () => window.chess.useEngine({ engine }));
  if (r?.ok) {
    usedEngineKey = JSON.stringify(engine);
    renderEngines(engineResult);
    await refreshEngineOptions();
  }
}

// ================= Engine options =================
let advancedOpen = false;
const pendingOptions = new Map();   // option name -> new value, sent by the Apply button

async function refreshEngineOptions() {
  renderEngineOptions(await window.chess.listCurrentEngineOptions({}));
}

function renderEngineOptions({ ok, presets = [], advanced = [] }) {
  const usable = presets.filter((p) => p.supported);
  pendingOptions.clear();
  $('engine-options').hidden = !(ok && (usable.length || advanced.length));

  $('preset-options').replaceChildren(
    el('div', { className: 'section-title', textContent: 'Presets' }),
    ...(usable.length
      ? usable.map(renderPreset)
      : [el('div', { className: 'empty', textContent: 'No presets available for this engine.' })]));

  $('btn-advanced').hidden = !advanced.length;
  $('advanced-list').replaceChildren(...advanced.map(renderOption));
  $('advanced-options').hidden = !advancedOpen;
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
      el('button', { textContent: p.name, onclick: () => sendOption(p.name) }));
  }
  const { input, read } = makeInput(p);
  input.addEventListener('change', () => pendingOptions.set(p.name, read()));
  return field(p, input);
}

// A preset is several options set together: one Apply sends all values in order.
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
    onclick: () => sendPreset(preset.label, reads.map((read) => read())),
  }));
  return box;
}

async function applyAdvanced() {
  if (!pendingOptions.size) return setMsg('No advanced option changed');
  await runBusy('Applying options…', async () => {
    for (const [property, value] of pendingOptions) {
      const r = await window.chess.setEngineOption({ property, value });
      if (!r?.ok) break;   // the failure notice is already shown; stop here
    }
  });
  refreshEngineOptions();   // re-sync the controls with the engine's real values
}

async function sendOption(property, value) {   // used by option buttons
  const r = await runBusy(null, () => window.chess.setEngineOption({ property, value }));
  if (!r?.ok) refreshEngineOptions();
}

async function sendPreset(preset, values) {
  const r = await runBusy('Applying…', () => window.chess.setEnginePreset({ preset, values }));
  if (!r?.ok) refreshEngineOptions();
}

// ================= Wiring =================
updateComputerButton();
$('btn-computer').addEventListener('click', () =>
  togglePanel('vs-computer', () => refreshEngines().then(refreshEngineOptions)));
$('vc-on').addEventListener('change', applyComputerSettings);
$('vc-side').addEventListener('change', applyComputerSettings);
$('btn-refresh-engines').addEventListener('click', refreshEngines);

$('btn-advanced').addEventListener('click', () => {
  advancedOpen = !advancedOpen;
  $('advanced-options').hidden = !advancedOpen;
  $('btn-advanced').textContent = advancedOpen ? 'Hide advanced options' : 'Show advanced options';
});
$('btn-apply-advanced').addEventListener('click', applyAdvanced);