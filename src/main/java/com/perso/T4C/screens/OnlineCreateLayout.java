package com.perso.T4C.screens;

/**
 * Render-free geometry of the online character-creation form, in panel-relative pixels (y grows
 * downwards). It lives on the same panel as the lobby and reuses its right-hand button column.
 */
final class OnlineCreateLayout {
  static final float LEFT_X = 16f;
  static final float LABEL_Y = 46f;
  static final float FIELD_Y = 66f;
  static final float FIELD_WIDTH = 250f;
  static final float FIELD_HEIGHT = 26f;

  static final float CLASS_LABEL_Y = 104f;
  static final float CLASS_Y = 124f;
  static final float CLASS_PITCH = 30f;
  static final float CLASS_WIDTH = 250f;
  static final float CLASS_HEIGHT = 23f;
  /** Rows that fit above the status line. */
  static final int MAX_CLASS_ROWS = 3;

  static final float MID_X = 292f;
  static final float GENDER_LABEL_Y = 46f;
  static final float GENDER_Y = 66f;
  static final float ALIGN_LABEL_Y = 104f;
  static final float ALIGN_Y = 124f;
  static final float SMALL_WIDTH = 72f;
  static final float SMALL_HEIGHT = 24f;
  static final float SMALL_GAP = 78f;

  static final float CREATE_Y = OnlineLobbyLayout.PLAY_Y;
  static final float BACK_Y = OnlineLobbyLayout.BACK_Y;

  enum Target {
    NAME,
    MALE,
    FEMALE,
    GOOD,
    EVIL,
    CREATE,
    BACK
  }

  private OnlineCreateLayout() {}

  /** Class row index under the pointer, or -1. */
  static int classAt(float px, float py, float panelX, float panelY, int classCount) {
    int rows = Math.min(classCount, MAX_CLASS_ROWS);
    for (int row = 0; row < rows; row++) {
      if (inside(px, py, panelX + LEFT_X, panelY + CLASS_Y + row * CLASS_PITCH, CLASS_WIDTH, CLASS_HEIGHT))
        return row;
    }
    return -1;
  }

  /** The non-class control under the pointer, or null. */
  static Target targetAt(float px, float py, float panelX, float panelY) {
    if (inside(px, py, panelX + LEFT_X, panelY + FIELD_Y, FIELD_WIDTH, FIELD_HEIGHT)) return Target.NAME;
    if (inside(px, py, panelX + MID_X, panelY + GENDER_Y, SMALL_WIDTH, SMALL_HEIGHT)) return Target.MALE;
    if (inside(px, py, panelX + MID_X + SMALL_GAP, panelY + GENDER_Y, SMALL_WIDTH, SMALL_HEIGHT))
      return Target.FEMALE;
    if (inside(px, py, panelX + MID_X, panelY + ALIGN_Y, SMALL_WIDTH, SMALL_HEIGHT)) return Target.GOOD;
    if (inside(px, py, panelX + MID_X + SMALL_GAP, panelY + ALIGN_Y, SMALL_WIDTH, SMALL_HEIGHT))
      return Target.EVIL;
    if (inside(px, py, panelX + OnlineLobbyLayout.BUTTON_X, panelY + CREATE_Y,
        OnlineLobbyLayout.BUTTON_WIDTH, OnlineLobbyLayout.BUTTON_HEIGHT))
      return Target.CREATE;
    if (inside(px, py, panelX + OnlineLobbyLayout.BUTTON_X, panelY + BACK_Y,
        OnlineLobbyLayout.BUTTON_WIDTH, OnlineLobbyLayout.BUTTON_HEIGHT))
      return Target.BACK;
    return null;
  }

  private static boolean inside(float px, float py, float x, float y, float w, float h) {
    return px >= x && px <= x + w && py >= y && py <= y + h;
  }
}
