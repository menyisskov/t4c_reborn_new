package com.perso.T4C.gui.core;

import com.badlogic.gdx.Input;

/** Shared confirmation treatment; closing always takes the cancellation path. */
public class JournalConfirmWindow extends JournalWindow {
  private final Runnable cancel;

  public JournalConfirmWindow(String title, String message, Runnable confirm, Runnable cancel) {
    this.cancel = cancel;
    initJournal(420, 140, title);
    journalValue(
        () -> com.perso.T4C.i18n.I18n.key(message), 28, 12, 364, 58, 14, JournalTheme.TEXT);
    journalButton(168, 90, 102, 32, "character.no", this::closeWindow);
    journalButton(282, 90, 110, 32, "character.yes", confirm);
  }

  @Override
  protected void closeWindow() {
    cancel.run();
  }

  @Override
  public boolean onKeyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE) {
      closeWindow();
      return true;
    }
    return false;
  }
}
