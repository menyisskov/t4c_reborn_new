package com.perso.T4C.gui.screen;

import com.badlogic.gdx.Gdx;
import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.JournalConfirmWindow;

public final class ExitGameConfirmScreen extends JournalConfirmWindow {
  public ExitGameConfirmScreen(OptionsScreen options) {
    super(
        "options.quit",
        "options.quit.confirm",
        () -> Gdx.app.exit(),
        () -> GuiManager.open(options.reopened()));
  }
}
