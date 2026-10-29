package com.perso.T4C.gui.screen;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.ui.RadarHud;
import org.junit.jupiter.api.Test;

/** Where the online lists sit: the centred journal window, top to bottom, clear of the radar. */
class OnlineListWindowLayoutTest {
  @Test
  void rowsRunTopToBottomInsideTheWindow() {
    float previous = -1;
    for (int row = 0; row < OnlineListWindow.MAX_BODY_ROWS; row++) {
      float top = OnlineListWindow.rowTop(row);
      assertTrue(top > previous, "row " + row + " is below the one before it");
      previous = top;
    }
    assertTrue(
        previous + OnlineListWindow.ROW_H <= OnlineListWindow.HINT_Y,
        "the hint sits under the last row");
    assertTrue(OnlineListWindow.HINT_Y + OnlineListWindow.ROW_H <= OnlineListWindow.HEIGHT);
  }

  @Test
  void theWindowIsCentredAndNeverCoversTheRadarOnCommonScreens() {
    int[][] screens = {{1280, 720}, {1366, 768}, {1600, 900}, {1920, 1080}, {2560, 1440}};
    for (int[] screen : screens) {
      float[] box = OnlineListWindow.bounds(screen[0], screen[1]);
      assertEquals((screen[0] - box[2]) / 2f, box[0], 1f, "centred across");
      assertTrue(
          box[0] >= 0
              && box[1] >= 0
              && box[0] + box[2] <= screen[0]
              && box[1] + box[3] <= screen[1]);
      for (float x = box[0]; x <= box[0] + box[2]; x += 8f) {
        for (float y = box[1]; y <= box[1] + box[3]; y += 8f) {
          assertFalse(
              RadarHud.contains(x, y, screen[0], screen[1]),
              screen[0] + "x" + screen[1] + " at " + x + "," + y);
        }
      }
    }
  }
}
