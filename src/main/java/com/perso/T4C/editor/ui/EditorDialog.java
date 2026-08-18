package com.perso.T4C.editor.ui;

public abstract class EditorDialog extends EditorComponent {
  private final String title;

  protected EditorDialog(String title) {
    this.title = title;
  }

  public String title() {
    return title;
  }

  public void close() {
    visible = false;
  }

  public boolean handleTouchUp(int screenX, int screenY, int button) {
    return false;
  }

  public boolean handleDrag(int screenX, int screenY) {
    return false;
  }
}
