package com.perso.T4C.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.TimeUtils;
import com.perso.T4C.audio.SoundManager;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiResizable;
import com.perso.T4C.ui.FontManager;
import java.util.Objects;

public class GuiButton extends AbstractGuiElement implements GuiResizable {
  private final TextureRegion normal;
  private final TextureRegion hover;
  private final TextureRegion pressed;
  private final Runnable callback;
  private boolean isHovered;
  private boolean isPressed;
  private boolean visible = true;
  private boolean enabled = true;
  private float renderedWidth;
  private float renderedHeight;
  private float labelVerticalOffset;
  private com.badlogic.gdx.graphics.g2d.BitmapFont labelFont;
  private java.util.function.Supplier<String> labelText;
  private final com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
      new com.badlogic.gdx.graphics.g2d.GlyphLayout();
  private static final float LABEL_PAD = 2f;
  // T4C-0054: press-and-hold repeat-fires the callback (e.g. skill/stat "+1" spin buttons) so
  // allocating a large batch of points doesn't take one click per point. No held-input timer
  // existed anywhere in this hand-rolled GUI, so this piggybacks on render() (already called
  // every frame) rather than adding a new update hook. Opt-in via repeatable(true) - a plain
  // toggle button (e.g. the spellbook's macro +/- button) must NOT repeat, since re-firing its
  // callback every 60ms would flip its state back and forth instead of incrementing anything.
  private static final long HOLD_REPEAT_DELAY_MS = 350L;
  private static final long HOLD_REPEAT_INTERVAL_MS = 60L;
  private boolean repeatable;
  private long pressStartMillis;
  private long lastRepeatMillis;
  private boolean repeatFired;

  public GuiButton repeatable(boolean repeatable) {
    this.repeatable = repeatable;
    return this;
  }

  /**
   * Carries an in-progress hold-and-repeat over from a button this one replaces at the same
   * screen slot (e.g. a spin button recreated by rebuildList() while the mouse is still held) -
   * otherwise the new instance's press/timing state starts blank and the hold silently stops
   * after the first repeat, since the replaced button never received the touch-up that would
   * normally end it.
   */
  public void adoptHoldStateFrom(GuiButton previous) {
    if (previous == null || !repeatable) {
      return;
    }
    this.isPressed = previous.isPressed;
    this.isHovered = previous.isHovered;
    this.pressStartMillis = previous.pressStartMillis;
    this.lastRepeatMillis = previous.lastRepeatMillis;
    this.repeatFired = previous.repeatFired;
  }

  public GuiButton withLabel(
      com.badlogic.gdx.graphics.g2d.BitmapFont font, java.util.function.Supplier<String> text) {
    this.labelFont = font;
    this.labelText = text;
    return this;
  }

  public GuiButton withLabelVerticalOffset(float offset) {
    this.labelVerticalOffset = offset;
    return this;
  }

  public GuiButton(
      TextureRegion normal,
      TextureRegion hover,
      TextureRegion pressed,
      float x,
      float y,
      Runnable callback) {
    super(x, y);
    this.normal = Objects.requireNonNull(normal);
    this.hover = Objects.requireNonNull(hover);
    this.pressed = Objects.requireNonNull(pressed);
    this.callback = callback;
    this.renderedWidth = normal.getRegionWidth();
    this.renderedHeight = normal.getRegionHeight();
  }

  @Override
  public GuiButton setSize(float width, float height) {
    this.renderedWidth = Math.max(1f, width);
    this.renderedHeight = Math.max(1f, height);
    return this;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (!enabled) {
      isHovered = false;
      isPressed = false;
    }
  }

  public void render(SpriteBatch batch) {
    fireHeldRepeat();
    if (!visible) {
      return;
    }
    TextureRegion region;
    if (!enabled) {
      region = normal;
    } else if (isPressed) {
      region = pressed;
    } else if (isHovered) {
      region = hover;
    } else {
      region = normal;
    }
    GuiDraw.drawRegionFlipped(batch, region, x, y, renderedWidth, renderedHeight);
    if (labelFont != null && labelText != null) {
      FontManager.getInstance().applyCurrentQuality(labelFont);
      String text = labelText.get();
      if (text != null && !text.isEmpty()) {
        float w = renderedWidth;
        float h = renderedHeight;
        float prevScaleX = labelFont.getData().scaleX;
        float prevScaleY = labelFont.getData().scaleY;
        layout.setText(labelFont, text);
        float maxW = w - 2f * LABEL_PAD;
        float maxH = h - 2f * LABEL_PAD;
        if (layout.width > maxW || layout.height > maxH) {
          float scale = Math.min(maxW / layout.width, maxH / layout.height);
          labelFont.getData().setScale(prevScaleX * scale, prevScaleY * scale);
          layout.setText(labelFont, text);
        }
        labelFont.draw(
            batch,
            layout,
            x + (w - layout.width) / 2f,
            y + (h - layout.height) / 2f + labelVerticalOffset);
        labelFont.getData().setScale(prevScaleX, prevScaleY);
      }
    }
    GuiBoxedItem.drawDebugBorder(batch, x, y, renderedWidth, renderedHeight);
  }

  public void onTouchDown(float screenX, float screenY) {
    isHovered = visible && enabled && contains(screenX, screenY);
    isPressed = isHovered;
    pressStartMillis = isPressed ? TimeUtils.millis() : 0L;
    lastRepeatMillis = 0L;
    repeatFired = false;
  }

  public void onTouchUp(float screenX, float screenY) {
    if (visible
        && enabled
        && isPressed
        && !repeatFired
        && contains(screenX, screenY)
        && callback != null) {
      SoundManager.interfaceSound("Generic pickup item.wav");
      callback.run();
    }
    isPressed = false;
    isHovered = visible && contains(screenX, screenY);
  }

  private void fireHeldRepeat() {
    if (!repeatable || !isPressed || !enabled || !visible || callback == null) {
      return;
    }
    if (!contains(Gdx.input.getX(), Gdx.input.getY())) {
      return;
    }
    long now = TimeUtils.millis();
    long elapsed = now - pressStartMillis;
    if (elapsed < HOLD_REPEAT_DELAY_MS) {
      return;
    }
    if (lastRepeatMillis != 0L && now - lastRepeatMillis < HOLD_REPEAT_INTERVAL_MS) {
      return;
    }
    lastRepeatMillis = now;
    repeatFired = true;
    callback.run();
  }

  public void onMouseMove(float screenX, float screenY) {
    if (!isPressed) {
      isHovered = visible && contains(screenX, screenY);
    }
  }

  public boolean contains(float screenX, float screenY) {
    return visible && containsBox(screenX, screenY);
  }

  @Override
  public float getWidth() {
    return renderedWidth;
  }

  @Override
  public float getHeight() {
    return renderedHeight;
  }
}
