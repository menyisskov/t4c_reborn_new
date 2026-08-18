package com.perso.T4C.gui.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.config.GameConstants;

public final class GuiDraw {
  private GuiDraw() {}

  public static void drawRegionFlipped(SpriteBatch batch, TextureRegion region, float x, float y) {
    drawRegionFlipped(batch, region, x, y, region.getRegionWidth(), region.getRegionHeight());
  }

  public static void drawRegionFlipped(
      SpriteBatch batch, TextureRegion region, float x, float y, float width, float height) {
    batch.draw(
        region.getTexture(),
        x,
        y,
        width,
        height,
        region.getRegionX(),
        region.getRegionY(),
        region.getRegionWidth(),
        region.getRegionHeight(),
        false,
        true);
  }

  public static float percentageToAlpha(float percentage) {
    if (!Float.isFinite(percentage)) return 0f;
    return Math.max(0f, Math.min(1f, percentage / 100f));
  }

  public static float overlayAlpha() {
    return com.perso.T4C.config.GamePreferencesStore.get().isTransparentGui()
        ? percentageToAlpha(GameConstants.OVERLAY_PERCENTAGE_DEFAULT)
        : 1f;
  }

  public static void drawOverlayRegionFlipped(
      SpriteBatch batch, TextureRegion region, float x, float y) {
    drawOverlayRegionFlipped(
        batch, region, x, y, region.getRegionWidth(), region.getRegionHeight());
  }

  public static void drawOverlayRegionFlipped(
      SpriteBatch batch, TextureRegion region, float x, float y, float width, float height) {
    withOverlayAlpha(batch, () -> drawRegionFlipped(batch, region, x, y, width, height));
  }

  public static void withOverlayAlpha(SpriteBatch batch, Runnable draw) {
    Color previous = new Color(batch.getColor());
    batch.setColor(previous.r, previous.g, previous.b, previous.a * overlayAlpha());
    try {
      draw.run();
    } finally {
      batch.setColor(previous);
    }
  }
}
