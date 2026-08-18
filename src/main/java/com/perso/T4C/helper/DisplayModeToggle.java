package com.perso.T4C.helper;

import com.badlogic.gdx.Gdx;
import com.perso.T4C.config.GameConstants;

public final class DisplayModeToggle {
  private DisplayModeToggle() {}

  public static boolean toggle() {
    if (Gdx.graphics.isFullscreen()) {
      Gdx.graphics.setWindowedMode(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
      return false;
    }
    setFullscreen();
    return true;
  }

  public static void setFullscreen() {
    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
  }

  public static String message(boolean fullscreen) {
    return fullscreen ? "Fullscreen mode enabled." : "Windowed mode enabled.";
  }
}
