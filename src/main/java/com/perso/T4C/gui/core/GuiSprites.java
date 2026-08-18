package com.perso.T4C.gui.core;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.SpriteLoader;

public final class GuiSprites {
  private GuiSprites() {}

  public static TextureRegion load(String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }
    try {
      return SpriteLoader.getInstance().getRegionFromSpriteName(name);
    } catch (GameException ignored) {
      return null;
    }
  }
}
