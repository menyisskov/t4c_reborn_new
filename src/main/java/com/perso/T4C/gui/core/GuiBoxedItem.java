package com.perso.T4C.gui.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.config.GameConstants;

public final class GuiBoxedItem {
  private static final float BORDER_THICKNESS = 2f;
  private static Texture pixel;

  private GuiBoxedItem() {}

  public static void drawDebugBorder(
      SpriteBatch batch, float x, float y, float width, float height) {
    if (!GameConstants.DEBUG_GUI_TEXT_BOUNDS || width <= 0f || height <= 0f) {
      return;
    }
    Texture px = pixel();
    float t = BORDER_THICKNESS;
    Color previous = new Color(batch.getColor());
    batch.setColor(Color.RED);
    batch.draw(px, x, y, width, t);
    batch.draw(px, x, y + height - t, width, t);
    batch.draw(px, x, y, t, height);
    batch.draw(px, x + width - t, y, t, height);
    batch.setColor(previous);
  }

  public static Texture pixel() {
    if (pixel == null) {
      Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
      pm.setColor(Color.WHITE);
      pm.fill();
      pixel = new Texture(pm);
      pm.dispose();
    }
    return pixel;
  }
}
