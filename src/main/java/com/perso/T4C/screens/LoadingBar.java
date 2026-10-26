package com.perso.T4C.screens;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.JournalTheme;
import com.perso.T4C.ui.FontManager;

/** Journal progress treatment shared by startup and character loading. */
final class LoadingBar {
  private final JournalTheme theme = new JournalTheme();
  private final TextureRegion frame = theme.windowFrame(400, 150);
  private final BitmapFont text =
      FontManager.getInstance().getTahomaFont(13, JournalTheme.TEXT, false);
  private final BitmapFont heading =
      FontManager.getInstance().getT4CBeaulieuFont(22, JournalTheme.GOLD);
  private final GlyphLayout layout = new GlyphLayout();

  void draw(SpriteBatch batch, float centerX, float barY, float progress, String title) {
    float clamped = Math.max(0, Math.min(1, progress));
    GuiDraw.drawRegionFlipped(batch, frame, centerX - 200, barY - 70);
    if (title != null) {
      layout.setText(heading, title);
      heading.draw(batch, layout, centerX - layout.width / 2, barY - 47);
    }
    theme.rect(batch, centerX - 156, barY, 312, 8, JournalTheme.LINE);
    if (clamped > 0) theme.rect(batch, centerX - 156, barY, 312 * clamped, 8, JournalTheme.GOLD);
    layout.setText(text, Math.round(clamped * 100) + " %");
    text.draw(batch, layout, centerX - layout.width / 2, barY + 26);
  }

  void dispose() {
    theme.dispose();
  }
}
