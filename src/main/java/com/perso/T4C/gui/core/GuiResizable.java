package com.perso.T4C.gui.core;

public interface GuiResizable extends GuiElement {
  float getWidth();

  float getHeight();

  GuiResizable setSize(float width, float height);
}
