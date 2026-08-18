package com.perso.T4C.gui.widget;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perso.T4C.gui.core.AbstractGuiElement;
import com.perso.T4C.gui.core.GuiBoxedItem;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.gui.core.GuiResizable;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class GuiCheckbox extends AbstractGuiElement implements GuiResizable {
  private final TextureRegion on;
  private final TextureRegion onHover;
  private final TextureRegion off;
  private final TextureRegion offHover;
  private final BooleanSupplier value;
  private final Consumer<Boolean> setter;
  private float width;
  private float height;
  private boolean hovered;
  private boolean pressed;

  public GuiCheckbox(
      TextureRegion on,
      TextureRegion onHover,
      TextureRegion off,
      TextureRegion offHover,
      float x,
      float y,
      BooleanSupplier value,
      Consumer<Boolean> setter) {
    super(x, y);
    this.on = on;
    this.onHover = onHover != null ? onHover : on;
    this.off = off;
    this.offHover = offHover != null ? offHover : off;
    this.value = value;
    this.setter = setter;
    this.width = on != null ? on.getRegionWidth() : 16f;
    this.height = on != null ? on.getRegionHeight() : 15f;
  }

  public GuiCheckbox boxed(float width, float height) {
    this.width = Math.max(1f, width);
    this.height = Math.max(1f, height);
    return this;
  }

  @Override
  public GuiCheckbox setSize(float width, float height) {
    return boxed(width, height);
  }

  @Override
  public void render(SpriteBatch batch) {
    boolean checked = value.getAsBoolean();
    TextureRegion region = checked ? (hovered ? onHover : on) : (hovered ? offHover : off);
    if (region != null) {
      GuiDraw.drawRegionFlipped(
          batch,
          region,
          x + (width - region.getRegionWidth()) / 2f,
          y + (height - region.getRegionHeight()) / 2f);
    }
    GuiBoxedItem.drawDebugBorder(batch, x, y, width, height);
  }

  @Override
  public void onTouchDown(float screenX, float screenY) {
    pressed = contains(screenX, screenY);
  }

  @Override
  public void onTouchUp(float screenX, float screenY) {
    if (pressed && contains(screenX, screenY)) setter.accept(!value.getAsBoolean());
    pressed = false;
  }

  @Override
  public void onMouseMove(float screenX, float screenY) {
    hovered = contains(screenX, screenY);
  }

  @Override
  public float getWidth() {
    return width;
  }

  @Override
  public float getHeight() {
    return height;
  }
}
