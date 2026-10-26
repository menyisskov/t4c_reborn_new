package com.perso.T4C.gui.core;

/** Layout manipulation is a developer tool, never an ordinary Ctrl/Shift interaction. */
public final class GuiLayoutEditing {
  private GuiLayoutEditing() {}

  public static boolean enabled() {
    return Boolean.getBoolean("t4c.gui.layoutEditing");
  }
}
