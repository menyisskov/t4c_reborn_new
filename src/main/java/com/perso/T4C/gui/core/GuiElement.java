package com.perso.T4C.gui.core;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface GuiElement {
  void render(SpriteBatch batch);

  boolean contains(float screenX, float screenY);

  void setPosition(float x, float y);

  float getX();

  float getY();

  default void onTouchDown(float screenX, float screenY) {}

  default void onTouchUp(float screenX, float screenY) {}

  default void onMouseMove(float screenX, float screenY) {}
}
