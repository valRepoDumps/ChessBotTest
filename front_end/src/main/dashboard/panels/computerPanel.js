// "Vs computer" panel: computer side, engine list, engine presets and advanced options.

export function createComputerPanel(ctx) {
  const { state, backend, push, runBusy } = ctx;
  const P = {
    engines: { loading: true },
    usedEngineKey: null,
    advancedOpen: false,
    options: { rev: 0, visible: false, presets: [], advanced: [] },
  };
  const pendingOptions = new Map();   // option name -> new value, sent by Apply

  function engineView() {
    const r = P.engines;
    if (r.loading || !r.ok) return { loading: !!r.loading, ok: r.ok, reason: r.reason };
    return {
      ok: true,
      list: r.engines.map((e) => ({
        name: e.name || e.path || 'Engine',
        author: e.author || '',
        inUse: JSON.stringify(e) === P.usedEngineKey,
      })),
    };
  }

  const view = () => ({ engines: engineView(), options: P.options, advancedOpen: P.advancedOpen });

  // ----- computer side -----
  function setComputer({ on, side }) {
    state.computerSide = on ? side : false;
    ctx.clearSelection();
    push();
    ctx.maybeComputerMove();   // if it's already the computer's turn it plays now
  }

  // ----- engines -----
  async function refreshEngines() {
    P.engines = { loading: true };
    push();
    const r = await backend.listEngines();
    if (!P.usedEngineKey && r.ok) {
      const builtin = r.engines.find((e) => e.builtin);   // built-in engine is the default
      if (builtin) P.usedEngineKey = JSON.stringify(builtin);
    }
    P.engines = r;
    push();
  }

  async function useEngine({ index }) {
    const engine = P.engines.engines?.[index];
    if (!engine) return;
    const r = await runBusy('Switching engine…', () => backend.useEngine({ engine }));
    if (r?.ok) {
      P.usedEngineKey = JSON.stringify(engine);
      push();
      await refreshEngineOptions();
    }
  }

  // ----- options -----
  async function refreshEngineOptions() {
    const r = await backend.listCurrentEngineOptions({});
    const presets = (r?.presets || []).filter((p) => p.supported);
    const advanced = r?.advanced || [];
    pendingOptions.clear();
    P.options = {
      rev: P.options.rev + 1,
      visible: !!(r?.ok && (presets.length || advanced.length)),
      presets, advanced,
    };
    push();
  }

  async function applyAdvanced() {
    if (!pendingOptions.size) { state.msg = 'No advanced option changed'; return push(); }
    await runBusy('Applying options…', async () => {
      for (const [property, value] of pendingOptions) {
        const r = await backend.setEngineOption({ property, value });
        if (!r?.ok) break;   // the failure notice is already shown; stop here
      }
    });
    refreshEngineOptions();   // re-sync the controls with the engine's real values
  }

  async function sendOption({ name }) {   // option buttons
    const r = await runBusy(null, () => backend.setEngineOption({ property: name, value: undefined }));
    if (!r?.ok) refreshEngineOptions();
  }

  async function applyPreset({ preset, values }) {
    const r = await runBusy('Applying…', () => backend.setEnginePreset({ preset, values }));
    if (!r?.ok) refreshEngineOptions();
  }

  const handlers = {
    'toggle-computer': () => ctx.togglePanel('vs-computer'),
    computer: setComputer,
    'refresh-engines': refreshEngines,
    'use-engine': useEngine,
    'toggle-advanced': () => { P.advancedOpen = !P.advancedOpen; push(); },
    'option-changed': ({ name, value }) => { pendingOptions.set(name, value); },
    'apply-advanced': applyAdvanced,
    'send-option': sendOption,
    'apply-preset': applyPreset,
  };

  return {
    onOpen: () => refreshEngines().then(refreshEngineOptions),
    view, handlers,
  };
}