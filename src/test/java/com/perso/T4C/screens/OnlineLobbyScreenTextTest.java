package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.i18n.I18n;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class OnlineLobbyScreenTextTest {
  @Test
  void everyServerAvailabilityHasAnEnglishLabel() {
    for (String availability :
        new String[] {"ready", "in_use", "uninitialized", "unhosted_region", "unavailable_respawn"}) {
      assertTrue(I18n.has("online.lobby.status." + availability), availability);
      assertNotEquals(availability, OnlineLobbyScreen.availabilityLabel(availability));
    }
  }

  @Test
  void knownClassesUseTheGameNameAndUnknownOnesAreShownAsSent() {
    assertEquals("Paladin", OnlineLobbyScreen.classLabel("paladin"));
    assertEquals("Paladin", OnlineLobbyScreen.classLabel("Paladin"));
    assertEquals("Void Knight", OnlineLobbyScreen.classLabel("Void Knight"));
    assertEquals("-", OnlineLobbyScreen.classLabel(null));
  }

  @Test
  void failuresAreDescribedInPlainEnglishWithoutRawExceptionText() {
    String text = OnlineLobbyScreen.describe(new IOException("secret-internal-detail"));
    assertEquals(I18n.key("online.lobby.error.unreachable"), text);
    assertFalse(text.contains("secret-internal-detail"));
    assertEquals(
        I18n.key("online.lobby.error.timeout"),
        OnlineLobbyScreen.describe(new InterruptedException()));
  }
}
