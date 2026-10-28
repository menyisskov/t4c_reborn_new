package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.OnlineCharacterCreation;
import com.perso.T4C.screens.OnlineCreateLayout.Target;
import com.perso.T4C.screens.OnlineLobbyLayout.Button;
import org.junit.jupiter.api.Test;

class OnlineCreateLayoutTest {
  private static Target at(float x, float y) {
    return OnlineCreateLayout.targetAt(x, y, 0f, 0f);
  }

  @Test
  void everyServerClassHasARowOnTheForm() {
    for (int i = 0; i < OnlineCharacterCreation.CLASSES.size(); i++) {
      float y = OnlineCreateLayout.CLASS_Y + i * OnlineCreateLayout.CLASS_PITCH + 5f;
      assertEquals(i, OnlineCreateLayout.classAt(OnlineCreateLayout.LEFT_X + 5f, y, 0f, 0f,
          OnlineCharacterCreation.CLASSES.size()));
    }
    assertEquals(-1, OnlineCreateLayout.classAt(OnlineCreateLayout.LEFT_X + 5f,
        OnlineCreateLayout.CLASS_Y + 2 * OnlineCreateLayout.CLASS_PITCH + 5f, 0f, 0f, 2));
    assertEquals(-1, OnlineCreateLayout.classAt(-5f, -5f, 0f, 0f, 2));
  }

  @Test
  void formFitsEveryClassWithinTheWindowWithoutOverlap() {
    int rows = OnlineCharacterCreation.CLASSES.size();
    assertTrue(OnlineCreateLayout.CLASS_HEIGHT + 3f <= OnlineCreateLayout.CLASS_PITCH);
    float lastBottom = OnlineCreateLayout.classBottom(rows);
    assertTrue(OnlineCreateLayout.statusLineY(rows) > lastBottom);
    assertTrue(OnlineCreateLayout.errorLineY(rows) > OnlineCreateLayout.statusLineY(rows));
    float height = OnlineCreateLayout.panelHeight(rows);
    assertTrue(height >= OnlineCreateLayout.errorLineY(rows) + 16f);
    // Same top edge as the lobby panel (y = (720 - 274) / 2 + 30): the taller form stays on screen.
    float top = (720f - OnlineLobbyLayout.PANEL_HEIGHT) / 2f + 30f;
    assertTrue(top + height <= 720f);
    // The action buttons and the gender/alignment choices stay inside the taller panel.
    assertTrue(OnlineCreateLayout.BACK_Y + OnlineLobbyLayout.BUTTON_HEIGHT <= lastBottom);
    assertTrue(OnlineCreateLayout.ALIGN_Y + OnlineCreateLayout.SMALL_HEIGHT <= lastBottom);
    // No class row overlaps the middle/right columns' controls horizontally.
    assertTrue(OnlineCreateLayout.LEFT_X + OnlineCreateLayout.CLASS_WIDTH < OnlineCreateLayout.MID_X);
  }

  @Test
  void panelGrowsWithTheClassCountAndNeverShrinksBelowTheLobby() {
    assertEquals(OnlineLobbyLayout.PANEL_HEIGHT, OnlineCreateLayout.panelHeight(0));
    assertTrue(OnlineCreateLayout.panelHeight(7) > OnlineCreateLayout.panelHeight(3));
  }

  @Test
  void controlsAreHitWhereTheyAreDrawn() {
    assertEquals(Target.NAME, at(OnlineCreateLayout.LEFT_X + 5f, OnlineCreateLayout.FIELD_Y + 5f));
    assertEquals(Target.MALE, at(OnlineCreateLayout.MID_X + 5f, OnlineCreateLayout.GENDER_Y + 5f));
    assertEquals(Target.FEMALE,
        at(OnlineCreateLayout.MID_X + OnlineCreateLayout.SMALL_GAP + 5f, OnlineCreateLayout.GENDER_Y + 5f));
    assertEquals(Target.GOOD, at(OnlineCreateLayout.MID_X + 5f, OnlineCreateLayout.ALIGN_Y + 5f));
    assertEquals(Target.EVIL,
        at(OnlineCreateLayout.MID_X + OnlineCreateLayout.SMALL_GAP + 5f, OnlineCreateLayout.ALIGN_Y + 5f));
    assertEquals(Target.CREATE, at(OnlineLobbyLayout.BUTTON_X + 5f, OnlineCreateLayout.CREATE_Y + 5f));
    assertEquals(Target.BACK, at(OnlineLobbyLayout.BUTTON_X + 5f, OnlineCreateLayout.BACK_Y + 5f));
    assertNull(at(300f, 250f));
  }

  @Test
  void panelOffsetShiftsEveryTarget() {
    assertEquals(Target.BACK,
        OnlineCreateLayout.targetAt(100f + OnlineLobbyLayout.BUTTON_X + 5f,
            50f + OnlineCreateLayout.BACK_Y + 5f, 100f, 50f));
  }

  @Test
  void smallChoicesDoNotOverlapEachOtherOrTheButtonColumn() {
    float genderRight = OnlineCreateLayout.MID_X + OnlineCreateLayout.SMALL_GAP + OnlineCreateLayout.SMALL_WIDTH;
    assertTrue(genderRight < OnlineLobbyLayout.BUTTON_X);
    assertTrue(OnlineCreateLayout.SMALL_WIDTH < OnlineCreateLayout.SMALL_GAP);
    assertTrue(OnlineCreateLayout.LEFT_X + OnlineCreateLayout.FIELD_WIDTH < OnlineCreateLayout.MID_X);
  }

  @Test
  void lobbyCreateButtonHasItsOwnSlot() {
    assertEquals(Button.CREATE, OnlineLobbyLayout.buttonAt(
        OnlineLobbyLayout.BUTTON_X + 5f, OnlineLobbyLayout.CREATE_Y + 5f, 0f, 0f));
    assertTrue(OnlineLobbyLayout.CREATE_Y >= OnlineLobbyLayout.REFRESH_Y + OnlineLobbyLayout.BUTTON_HEIGHT);
    assertTrue(OnlineLobbyLayout.CREATE_Y + OnlineLobbyLayout.BUTTON_HEIGHT <= OnlineLobbyLayout.BACK_Y);
  }
}
