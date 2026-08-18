package com.perso.T4C.editor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class EditorButton extends EditorControl {
  private String label;
  private final Runnable action;
  private Color background = new Color(0.12f, 0.45f, 0.78f, 1f);
  private Color disabledBackground = new Color(0.18f, 0.20f, 0.23f, 0.8f);
  private Color textColor = Color.WHITE;
  private BitmapFont font;

  public EditorButton(String label, Runnable action) {
    this.label = label;
    this.action = action;
  }

  public EditorButton withLabel(String label) {
    this.label = label;
    return this;
  }

  public EditorButton withFont(BitmapFont font) {
    this.font = font;
    return this;
  }

  public EditorButton withColors(Color background, Color textColor) {
    this.background = background;
    this.textColor = textColor;
    return this;
  }

  @Override
  public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
    if (!visible) {
      return;
    }
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(enabled ? background : disabledBackground);
    shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    shapeRenderer.end();
    if (font == null || batch == null) {
      return;
    }
    batch.begin();
    GlyphLayout layout = new GlyphLayout(font, label);
    font.setColor(textColor);
    font.draw(
        batch,
        label,
        bounds.x + (bounds.width - layout.width) * 0.5f,
        bounds.y + (bounds.height + layout.height) * 0.5f + 1f);
    batch.end();
  }

  @Override
  public boolean handleClick(int screenX, int screenY, int button) {
    if (!enabled || button != Input.Buttons.LEFT || !contains(screenX, screenY)) {
      return false;
    }
    if (action != null) {
      action.run();
    }
    return true;
  }
}
