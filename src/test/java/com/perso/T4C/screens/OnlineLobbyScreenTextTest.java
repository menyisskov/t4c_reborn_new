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

  @Test
  void aRefusedJoinGetsTheSpecificTextOnlyWhenTheFreshRosterRowExplainsIt() {
    String general = I18n.key("online.lobby.error.join_refused");
    assertEquals(I18n.key("online.lobby.error.in_use"), OnlineLobbyScreen.refineJoinNotice(general, "in_use"));
    String offline = OnlineLobbyScreen.refineJoinNotice(general, "unhosted_region");
    assertTrue(offline.contains(OnlineLobbyScreen.availabilityLabel("unhosted_region")), offline);
    assertTrue(OnlineLobbyScreen.refineJoinNotice(general, "unavailable_respawn").contains("Respawning"));
    // A ready row, no row (the character left the roster) and every other message stay as they are.
    assertEquals(general, OnlineLobbyScreen.refineJoinNotice(general, "ready"));
    assertEquals(general, OnlineLobbyScreen.refineJoinNotice(general, null));
    String incompatible = I18n.key("online.lobby.error.incompatible");
    assertEquals(incompatible, OnlineLobbyScreen.refineJoinNotice(incompatible, "in_use"));
  }

  @Test
  void anIncompatibleVersionIsNamedInTheLobby() {
    String expected = I18n.key("online.lobby.error.incompatible");
    assertEquals(
        expected,
        OnlineLobbyScreen.describe(new com.perso.T4C.online.OnlineAccountClient.Rejected(426, "protocol")));
    assertEquals(
        expected, OnlineLobbyScreen.describe(new com.perso.T4C.online.OnlineAccountClient.Incompatible()));
  }
}
