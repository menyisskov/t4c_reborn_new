package com.perso.T4C.entity;

public interface Nameable {
  long NAME_DISPLAY_MS = 4000L;

  String getName();

  void showNameFor(long ms);

  boolean isNameVisible();

  boolean isMouseOver(float mouseX, float mouseY);

  default void showName() {
    showNameFor(NAME_DISPLAY_MS);
  }
}
