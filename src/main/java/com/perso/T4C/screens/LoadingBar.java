package com.perso.T4C.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.ui.FontManager;

/**
 * Framed progress bar with a title above and a percentage below, shared by the startup screen and
 * the character loading screen. Draws into an already-begun batch with a y-down projection.
 */
final class LoadingBar {
  private static final float BAR_WIDTH = 314f;
  private static final float BAR_HEIGHT = 12f;
  private static final float FRAME_WIDTH = 360f;
  private static final float FRAME_HEIGHT = 26f;
  private final BitmapFont font;
  private final GlyphLayout layout = new GlyphLayout();
  private final TextureRegion emptyBar;
  private final TextureRegion progressBar;
  private final TextureRegion progressFrame;

  LoadingBar() {
    this.font = FontManager.getInstance().getT4CBeaulieuFont(22, Color.WHITE);
    this.emptyBar = loadSprite("GUI_BackChStat_Empty");
    this.progressBar = loadSprite("GUI_BackChStat_XP");
    this.progressFrame = loadSprite("64kTameProgressFrame");
  }

  private static TextureRegion loadSprite(String name) {
    try {
      TextureRegion region = SpriteLoader.getInstance().getRegionFromSpriteName(name);
      if (region == null) throw new IllegalStateException("Missing loading-bar sprite: " + name);
      return region;
    } catch (Exception error) {
      throw new IllegalStateException("Unable to load loading-bar sprite: " + name, error);
    }
  }

  /** Draws the bar with its top edge at {@code barY}, centred on {@code centerX}. */
  void draw(SpriteBatch batch, float centerX, float barY, float progress, String title) {
    float clamped = Math.max(0f, Math.min(1f, progress));
    float barX = centerX - BAR_WIDTH / 2f;
    GuiDraw.drawRegionFlipped(
        batch,
        progressFrame,
        centerX - FRAME_WIDTH / 2f,
        barY - (FRAME_HEIGHT - BAR_HEIGHT) / 2f,
        FRAME_WIDTH,
        FRAME_HEIGHT);
    GuiDraw.drawRegionFlipped(batch, emptyBar, barX, barY, BAR_WIDTH, BAR_HEIGHT);
    if (clamped > 0f) {
      GuiDraw.drawRegionFlipped(batch, progressBar, barX, barY, BAR_WIDTH * clamped, BAR_HEIGHT);
    }
    if (title != null) {
      layout.setText(font, title);
      font.draw(batch, title, centerX - layout.width / 2f, barY - 38f);
    }
    String percent = Math.round(clamped * 100f) + " %";
    layout.setText(font, percent);
    font.draw(batch, percent, centerX - layout.width / 2f, barY + BAR_HEIGHT + 14f);
  }
}
