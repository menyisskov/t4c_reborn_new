package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.screens.OnlineLobbyLayout.Button;
import org.junit.jupiter.api.Test;

class OnlineLobbyLayoutTest {
  private static final float PX = 100f;
  private static final float PY = 50f;

  @Test
  void rowsAreHitInsideTheirBandOnly() {
    float y0 = PY + OnlineLobbyLayout.ROW_Y;
    assertEquals(0, OnlineLobbyLayout.rowAt(PX + 20, y0 + 5, PX, PY, 0, 3));
    assertEquals(1, OnlineLobbyLayout.rowAt(PX + 20, y0 + OnlineLobbyLayout.ROW_PITCH + 5, PX, PY, 0, 3));
    // The gap between rows and the area left of the list are not rows.
    assertEquals(-1, OnlineLobbyLayout.rowAt(PX + 20, y0 + OnlineLobbyLayout.ROW_HEIGHT + 3, PX, PY, 0, 3));
    assertEquals(-1, OnlineLobbyLayout.rowAt(PX + 2, y0 + 5, PX, PY, 0, 3));
    // A row slot with no character behind it is empty space.
    assertEquals(-1, OnlineLobbyLayout.rowAt(PX + 20, y0 + 2 * OnlineLobbyLayout.ROW_PITCH + 5, PX, PY, 0, 2));
  }

  @Test
  void rowIndexAccountsForScrollOffset() {
    float y0 = PY + OnlineLobbyLayout.ROW_Y;
    assertEquals(6, OnlineLobbyLayout.rowAt(PX + 20, y0 + 5, PX, PY, 6, 10));
    assertEquals(9, OnlineLobbyLayout.rowAt(PX + 20, y0 + 3 * OnlineLobbyLayout.ROW_PITCH + 5, PX, PY, 6, 10));
  }

  @Test
  void buttonsAndArrowsAreDistinct() {
    assertEquals(Button.PLAY, hit(OnlineLobbyLayout.BUTTON_X + 5, OnlineLobbyLayout.PLAY_Y + 5));
    assertEquals(Button.REFRESH, hit(OnlineLobbyLayout.BUTTON_X + 5, OnlineLobbyLayout.REFRESH_Y + 5));
    assertEquals(Button.BACK, hit(OnlineLobbyLayout.BUTTON_X + 5, OnlineLobbyLayout.BACK_Y + 5));
    assertEquals(Button.UP, hit(OnlineLobbyLayout.ARROW_X + 5, OnlineLobbyLayout.ARROW_UP_Y + 5));
    assertEquals(Button.DOWN, hit(OnlineLobbyLayout.ARROW_X + 5, OnlineLobbyLayout.ARROW_DOWN_Y + 5));
    assertNull(hit(5, 5));
  }

  @Test
  void controlsStayInsideThePanel() {
    assertTrue(OnlineLobbyLayout.BUTTON_X + OnlineLobbyLayout.BUTTON_WIDTH <= OnlineLobbyLayout.PANEL_WIDTH);
    assertTrue(OnlineLobbyLayout.ROW_X + OnlineLobbyLayout.ROW_WIDTH < OnlineLobbyLayout.ARROW_X);
    assertTrue(OnlineLobbyLayout.ARROW_X + OnlineLobbyLayout.ARROW_WIDTH < OnlineLobbyLayout.BUTTON_X);
    assertTrue(OnlineLobbyLayout.NOTE_Y + 16 <= OnlineLobbyLayout.PANEL_HEIGHT);
  }

  @Test
  void scrollKeepsSelectionVisibleAndClamped() {
    assertEquals(0, OnlineLobbyLayout.scrollToShow(0, 3, 10));
    assertEquals(1, OnlineLobbyLayout.scrollToShow(0, 4, 10));
    assertEquals(6, OnlineLobbyLayout.scrollToShow(6, 9, 10));
    assertEquals(2, OnlineLobbyLayout.scrollToShow(5, 2, 10));
    // A shrunken list pulls the window back; nothing selected leaves it where it fits.
    assertEquals(0, OnlineLobbyLayout.scrollToShow(8, -1, 3));
    assertEquals(4, OnlineLobbyLayout.scrollToShow(9, -1, 8));
  }

  @Test
  void leaseSecondsRoundUpSoABlockedPlayNeverSaysZero() {
    assertEquals(0, OnlineLobbyLayout.secondsCeil(0));
    assertEquals(0, OnlineLobbyLayout.secondsCeil(-5));
    assertEquals(1, OnlineLobbyLayout.secondsCeil(1));
    assertEquals(1, OnlineLobbyLayout.secondsCeil(1000));
    assertEquals(2, OnlineLobbyLayout.secondsCeil(1001));
  }

  private static Button hit(float relX, float relY) {
    return OnlineLobbyLayout.buttonAt(PX + relX, PY + relY, PX, PY);
  }
}
