package com.perso.T4C.gui.core;

/** A GUI element whose boxed hit zone can be resized interactively. */
public interface GuiResizable extends GuiElement {
    float getWidth();

    float getHeight();

    GuiResizable setSize(float width, float height);
}
