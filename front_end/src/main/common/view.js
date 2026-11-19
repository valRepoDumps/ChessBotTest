// Builds the view object the renderer draws and pushes it over the 'view' channel.
// `panels` is filled in by the game after creation; it is only read when a view is built.

export function createView(S, send, panels) {
  function build() {
    const { editor, saves, 'vs-computer': computer } = panels;
    return {
      board: S.board, highlights: S.highlights, selected: S.selected,
      busy: S.busy, mode: editor.isActive() ? 'edit' : 'play', msg: S.msg, connected: S.connected,
      panel: S.panel, computerSide: S.computerSide, flags: S.flags,
      promotion: S.promotion,
      ...editor.view(), ...saves.view(), ...computer.view(),
    };
  }

  function push() { send('view', build()); }

  return { build, push };
}