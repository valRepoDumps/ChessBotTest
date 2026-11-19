// Saved-games panel. Owns the save list and the "clear the name box" revision counter.

export function createSavesPanel(ctx) {
  const { state, backend, push, runBusy } = ctx;
  const P = {
    saves: { ok: true, loading: true },
    saveNameRev: 0,   // bumped after a successful save so the renderer clears the name box
  };

  function refresh() {
    P.saves = { ok: true, loading: true };
    push();
    backend.listSaves();   // result arrives through onSaves
  }

  // pushed by the backend
  function onSaves(msg) {
    P.saves = { ...msg, loading: false };
    if (msg.saved) P.saveNameRev++;
    push();
  }

  async function loadGame({ save_id }) {
    const r = await runBusy('Loading…',
      () => backend.loadGame({ save_id, computer_side: state.computerSide }));
    if (r?.ok) await ctx.maybeComputerMove();
  }

  const view = () => ({ saves: P.saves, saveNameRev: P.saveNameRev });

  const handlers = {
    'toggle-saves': () => ctx.togglePanel('saves'),
    'refresh-saves': refresh,
    save: ({ name }) => runBusy(null, () => backend.saveGame({ name: name || 'Untitled' })),
    load: loadGame,
    'delete-save': ({ save_id }) => runBusy('Deleting…', () => backend.deleteSave({ save_id })),
  };

  return { onOpen: refresh, onSaves, view, handlers };
}