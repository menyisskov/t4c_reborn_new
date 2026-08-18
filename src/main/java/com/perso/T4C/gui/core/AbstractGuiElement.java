package com.perso.T4C.gui.core;

import lombok.Getter;

public abstract class AbstractGuiElement implements GuiElement {
  @Getter protected float x;
  @Getter protected float y;

  protected AbstractGuiElement(float x, float y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public void setPosition(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public float getWidth() {
    return 0f;
  }

  public float getHeight() {
    return 0f;
  }

  protected final boolean containsBox(float screenX, float screenY) {
    float width = getWidth();
    float height = getHeight();
    if (width <= 0f || height <= 0f) {
      return false;
    }
    return screenX >= x && screenX <= x + width && screenY >= y && screenY <= y + height;
  }

  @Override
  public boolean contains(float screenX, float screenY) {
    return containsBox(screenX, screenY);
  }
}
