//Opens, closes and toggles the side panels (editor, saves, vs-computer).
//A panel may define onOpen() and onClose({ restore }).

export function createPanelManager(S, panels, push) {
  function openPanel(name) {
    if (S.panel) panels[S.panel].onClose?.({ restore: true });
    S.panel = name;
    panels[name].onOpen?.();
    push();
  }

  function closePanel(restore) {
    const p = panels[S.panel];
    S.panel = null;
    p?.onClose?.({ restore });
    push();
  }

  function togglePanel(name) {
    if (S.panel === name) closePanel(true);
    else openPanel(name);
  }

  return { openPanel, closePanel, togglePanel };
}