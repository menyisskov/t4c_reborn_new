package com.perso.T4C.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.ui.FontManager;

public class GuiMapZoneDisplay {
  private static final float TOTAL_DURATION = 7.0f;
  private static final float FADE_DURATION = 1.8f;
  private final GuiText headerText;
  private final Color textBaseColor = Color.valueOf("F2B705");
  private final Color outlineBaseColor = new Color(0f, 0f, 0f, 1f);
  private final float x;
  private final float y;
  private float timer = 0f;

  public GuiMapZoneDisplay(String zoneName) {
    BitmapFont font = FontManager.getInstance().getT4CBeaulieuFont(32, textBaseColor);
    GlyphLayout layout = new GlyphLayout(font, zoneName);
    x = (Gdx.graphics.getWidth() - layout.width) * 0.5f;
    y = Gdx.graphics.getHeight() * 0.15f;
    float textX = x;
    float textY = y;
    headerText = new GuiText(font, textX, textY, () -> zoneName);
  }

  public boolean render(SpriteBatch batch, float delta) {
    timer += delta;
    if (timer >= TOTAL_DURATION) {
      return false;
    }
    float alpha = computeAlpha(timer);
    Color textColor = new Color(textBaseColor.r, textBaseColor.g, textBaseColor.b, alpha);
    Color outlineColor =
        new Color(outlineBaseColor.r, outlineBaseColor.g, outlineBaseColor.b, alpha);
    headerText.renderOutlined(batch, textColor, outlineColor, 1.5f);
    return true;
  }

  private float computeAlpha(float time) {
    float fade = Math.min(FADE_DURATION, TOTAL_DURATION * 0.5f);
    if (fade <= 0f) {
      return 1f;
    }
    if (time < fade) {
      return time / fade;
    }
    float remaining = TOTAL_DURATION - time;
    if (remaining < fade) {
      return Math.max(0f, remaining / fade);
    }
    return 1f;
  }
}
