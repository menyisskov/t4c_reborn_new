package com.perso.T4C.gui.core;

public final class GuiClickZone {
  private final float x;
  private final float y;
  private final float width;
  private final float height;
  private final Runnable action;

  public GuiClickZone(float x, float y, float width, float height, Runnable action) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.action = action;
  }

  public boolean contains(float screenX, float screenY) {
    return screenX >= x && screenX <= x + width && screenY >= y && screenY <= y + height;
  }

  public void run() {
    action.run();
  }
}
