package com.perso.T4C.gui.screen;

import com.perso.T4C.gui.core.GuiManager;
import com.perso.T4C.gui.core.JournalConfirmWindow;

public final class SwitchCharacterConfirmScreen extends JournalConfirmWindow {
  public SwitchCharacterConfirmScreen(Runnable action) {
    super(
        "options.switch_character",
        "options.switch_character.confirm",
        () -> {
          if (action != null) action.run();
        },
        () -> GuiManager.open(new OptionsScreen(action)));
  }
}
