package com.perso.T4C.screens;

/**
 * Render-free geometry of the online lobby panel, in panel-relative pixels (y grows downwards, like
 * {@link CharacterSelectionScreen}). Kept separate from the screen so hit-testing can be unit tested
 * without a GL context. Control sizes and offsets follow the single-player character screen.
 */
final class OnlineLobbyLayout {
  static final int VISIBLE_ROWS = 4;
  static final float PANEL_WIDTH = 604f;
  static final float PANEL_HEIGHT = 274f;

  static final float ROW_X = 16f;
  static final float ROW_Y = 76f;
  static final float ROW_PITCH = 30f;
  static final float ROW_WIDTH = 400f;
  static final float ROW_HEIGHT = 23f;

  static final float ARROW_X = 426f;
  static final float ARROW_UP_Y = 76f;
  static final float ARROW_DOWN_Y = 170f;
  static final float ARROW_WIDTH = 20f;
  static final float ARROW_HEIGHT = 21f;

  static final float BUTTON_X = 470f;
  static final float BUTTON_WIDTH = 116f;
  static final float BUTTON_HEIGHT = 27f;
  static final float PLAY_Y = 67f;
  static final float REFRESH_Y = 99f;
  static final float BACK_Y = 177f;

  /** Column left edges (row-relative text offsets) for name, class, level and availability. */
  static final float NAME_X = 10f;
  static final float CLASS_X = 160f;
  static final float LEVEL_X = 262f;
  static final float STATUS_X = 310f;

  static final float STATUS_LINE_Y = 208f;
  static final float ERROR_LINE_Y = 230f;
  static final float NOTE_Y = 252f;

  enum Button {
    PLAY,
    REFRESH,
    BACK,
    UP,
    DOWN
  }

  private OnlineLobbyLayout() {}

  /** Index into the entry list of the row under the pointer, or -1. */
  static int rowAt(float px, float py, float panelX, float panelY, int firstVisible, int entryCount) {
    for (int row = 0; row < VISIBLE_ROWS; row++) {
      int index = firstVisible + row;
      if (index >= entryCount) break;
      if (inside(px, py, panelX + ROW_X, panelY + ROW_Y + row * ROW_PITCH, ROW_WIDTH, ROW_HEIGHT))
        return index;
    }
    return -1;
  }

  /** The button under the pointer, or null. */
  static Button buttonAt(float px, float py, float panelX, float panelY) {
    if (inside(px, py, panelX + BUTTON_X, panelY + PLAY_Y, BUTTON_WIDTH, BUTTON_HEIGHT))
      return Button.PLAY;
    if (inside(px, py, panelX + BUTTON_X, panelY + REFRESH_Y, BUTTON_WIDTH, BUTTON_HEIGHT))
      return Button.REFRESH;
    if (inside(px, py, panelX + BUTTON_X, panelY + BACK_Y, BUTTON_WIDTH, BUTTON_HEIGHT))
      return Button.BACK;
    if (inside(px, py, panelX + ARROW_X, panelY + ARROW_UP_Y, ARROW_WIDTH, ARROW_HEIGHT))
      return Button.UP;
    if (inside(px, py, panelX + ARROW_X, panelY + ARROW_DOWN_Y, ARROW_WIDTH, ARROW_HEIGHT))
      return Button.DOWN;
    return null;
  }

  /** First visible row that keeps {@code selectedIndex} on screen, clamped to the list. */
  static int scrollToShow(int firstVisible, int selectedIndex, int entryCount) {
    int maxFirst = Math.max(0, entryCount - VISIBLE_ROWS);
    int first = Math.max(0, Math.min(firstVisible, maxFirst));
    if (selectedIndex < 0) return first;
    if (selectedIndex < first) first = selectedIndex;
    if (selectedIndex >= first + VISIBLE_ROWS) first = selectedIndex - VISIBLE_ROWS + 1;
    return Math.max(0, Math.min(first, maxFirst));
  }

  /** Whole seconds to display for a remaining hold: rounded up so "0 s" never shows while blocked. */
  static long secondsCeil(long millis) {
    return millis <= 0 ? 0 : (millis + 999) / 1000;
  }

  private static boolean inside(float px, float py, float x, float y, float w, float h) {
    return px >= x && px <= x + w && py >= y && py <= y + h;
  }
}
