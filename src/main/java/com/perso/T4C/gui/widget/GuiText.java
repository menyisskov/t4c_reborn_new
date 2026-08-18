package com.perso.T4C.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;
import java.util.Objects;
import java.util.function.Supplier;

public class GuiText extends AbstractGuiElement {
  private static final Color T4C_GOLD = Color.valueOf("F2B705");
  protected final BitmapFont font;
  protected final Supplier<String> textSupplier;
  protected final Supplier<Color> colorSupplier;

  public GuiText(BitmapFont font, float x, float y, Supplier<String> textSupplier) {
    this(font, x, y, textSupplier, null);
  }

  public GuiText(
      BitmapFont font,
      float x,
      float y,
      Supplier<String> textSupplier,
      Supplier<Color> colorSupplier) {
    super(x, y);
    this.font = Objects.requireNonNull(font);
    this.textSupplier = Objects.requireNonNull(textSupplier);
    this.colorSupplier = colorSupplier;
  }

  public static GuiText header(String text, float x, float y) {
    return new GuiText(
        FontManager.getInstance().getHaettenschweilerFont(18, T4C_GOLD), x, y, () -> text);
  }

  public static GuiText information(String text, float x, float y) {
    return information(() -> text, x, y);
  }

  public static GuiText translatedHeader(String key, String english, float x, float y) {
    return new GuiText(
        FontManager.getInstance().getHaettenschweilerFont(18, T4C_GOLD), x, y, () -> I18n.key(key));
  }

  public static GuiText information(Supplier<String> text, float x, float y) {
    return information(text, x, y, 11, T4C_GOLD);
  }

  public static GuiText information(
      Supplier<String> text, float x, float y, int fontSize, Color color) {
    return new GuiText(
        FontManager.getInstance().getJetBrainsMonoFont(fontSize, color), x, y, text, () -> color);
  }

  @Override
  public void render(SpriteBatch batch) {
    String text = textSupplier.get();
    if (text == null || text.isEmpty()) {
      return;
    }
    FontManager.getInstance().applyCurrentQuality(font);
    if (colorSupplier != null) {
      Color previous = new Color(font.getColor());
      font.setColor(colorSupplier.get());
      font.draw(batch, text, x, y);
      font.setColor(previous);
    } else {
      font.draw(batch, text, x, y);
    }
  }

  public void render(SpriteBatch batch, Color color) {
    String text = textSupplier.get();
    if (text == null || text.isEmpty()) {
      return;
    }
    FontManager.getInstance().applyCurrentQuality(font);
    Color previous = new Color(font.getColor());
    font.setColor(color);
    font.draw(batch, text, x, y);
    font.setColor(previous);
  }

  public void renderOutlined(
      SpriteBatch batch, Color textColor, Color outlineColor, float outlineOffset) {
    String text = textSupplier.get();
    if (text == null || text.isEmpty()) {
      return;
    }
    FontManager.getInstance().applyCurrentQuality(font);
    Color previous = new Color(font.getColor());
    font.setColor(outlineColor);
    font.draw(batch, text, x - outlineOffset, y);
    font.draw(batch, text, x + outlineOffset, y);
    font.draw(batch, text, x, y - outlineOffset);
    font.draw(batch, text, x, y + outlineOffset);
    font.setColor(textColor);
    font.draw(batch, text, x, y);
    font.setColor(previous);
  }

  @Override
  public boolean contains(float screenX, float screenY) {
    String text = textSupplier.get();
    if (text == null || text.isEmpty()) {
      return false;
    }
    GlyphLayout layout = new GlyphLayout(font, text);
    float left = x;
    float right = x + layout.width;
    float top = y;
    float bottom = y - layout.height;
    return screenX >= left && screenX <= right && screenY >= bottom && screenY <= top;
  }
}
