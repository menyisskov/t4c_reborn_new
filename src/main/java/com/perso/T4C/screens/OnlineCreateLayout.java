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
  /** Gap between the last class row and the status line, and between status and error lines. */
  static final float FOOTER_GAP = 10f;
  static final float FOOTER_LINE_PITCH = 22f;
  static final float FOOTER_PAD = 26f;

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

  /** Bottom edge of the last class row; there is one row per class, however many the server has. */
  static float classBottom(int classCount) {
    return CLASS_Y + Math.max(0, classCount - 1) * CLASS_PITCH + CLASS_HEIGHT + 3f;
  }

  /** Status ("working") line: below the class list, never above the lobby's own status line. */
  static float statusLineY(int classCount) {
    return Math.max(OnlineLobbyLayout.STATUS_LINE_Y, classBottom(classCount) + FOOTER_GAP);
  }

  static float errorLineY(int classCount) {
    return Math.max(OnlineLobbyLayout.ERROR_LINE_Y, statusLineY(classCount) + FOOTER_LINE_PITCH);
  }

  /** The panel grows downwards to hold every class row; its top edge stays where the lobby's is. */
  static float panelHeight(int classCount) {
    return Math.max(OnlineLobbyLayout.PANEL_HEIGHT, errorLineY(classCount) + FOOTER_PAD);
  }

  /** Class row index under the pointer, or -1. */
  static int classAt(float px, float py, float panelX, float panelY, int classCount) {
    for (int row = 0; row < classCount; row++) {
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
