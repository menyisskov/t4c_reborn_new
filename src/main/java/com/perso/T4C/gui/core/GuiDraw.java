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

  /**
   * Draws a window background with one rectangle of its art replaced by another (tiled) piece of
   * the same art, all at the window's overlay transparency. Used to hide decorations a screen
   * doesn't use (e.g. empty icon circles) without an opaque patch that stands out when the
   * interface is transparent. Coordinates are relative to the background.
   */
  public static void drawOverlayWithPatch(
      SpriteBatch batch,
      TextureRegion background,
      float x,
      float y,
      int holeX,
      int holeY,
      int holeW,
      int holeH,
      int srcX,
      int srcY,
      int srcW,
      int srcH) {
    int w = background.getRegionWidth();
    int h = background.getRegionHeight();
    TextureRegion piece = new TextureRegion();
    withOverlayAlpha(
        batch,
        () -> {
          // Top, bottom, left and right of the hole.
          drawPiece(batch, piece, background, 0, 0, w, holeY, x, y);
          drawPiece(batch, piece, background, 0, holeY + holeH, w, h - holeY - holeH, x, y);
          drawPiece(batch, piece, background, 0, holeY, holeX, holeH, x, y);
          drawPiece(
              batch, piece, background, holeX + holeW, holeY, w - holeX - holeW, holeH, x, y);
          // Fill the hole by tiling the source piece at native size.
          for (int ty = 0; ty < holeH; ty += srcH) {
            int th = Math.min(srcH, holeH - ty);
            for (int tx = 0; tx < holeW; tx += srcW) {
              int tw = Math.min(srcW, holeW - tx);
              piece.setRegion(background, srcX, srcY, tw, th);
              drawRegionFlipped(batch, piece, x + holeX + tx, y + holeY + ty, tw, th);
            }
          }
        });
  }

  private static void drawPiece(
      SpriteBatch batch,
      TextureRegion piece,
      TextureRegion background,
      int sx,
      int sy,
      int sw,
      int sh,
      float x,
      float y) {
    if (sw <= 0 || sh <= 0) return;
    piece.setRegion(background, sx, sy, sw, sh);
    drawRegionFlipped(batch, piece, x + sx, y + sy, sw, sh);
  }
}
