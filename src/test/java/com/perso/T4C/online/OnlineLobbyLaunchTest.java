package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.OnlineLobbyLaunch.Mode;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineLobbyLaunchTest {
  private static final String KEY = "b".repeat(64);

  @Test
  void defaultLaunchIsDirectAndArgumentsAreUntouched() {
    String[] args = {"http://127.0.0.1:18765"};
    assertFalse(OnlineLobbyLaunch.requested(null, null, args));
    assertFalse(OnlineLobbyLaunch.requested("false", "0", new String[0]));
    assertFalse(OnlineLobbyLaunch.requested("", "", null));
    assertArrayEquals(args, OnlineLobbyLaunch.positional(args));
    assertEquals(Mode.DIRECT, OnlineLobbyLaunch.select(false, true));
    assertEquals(Mode.DIRECT, OnlineLobbyLaunch.select(false, false));
  }

  @Test
  void anyExplicitSwitchRequestsTheLobby() {
    assertTrue(OnlineLobbyLaunch.requested("true", null, new String[0]));
    assertTrue(OnlineLobbyLaunch.requested(null, " 1 ", new String[0]));
    assertTrue(OnlineLobbyLaunch.requested(null, null, new String[] {"--lobby", "http://x"}));
    assertArrayEquals(
        new String[] {"https://example.test"},
        OnlineLobbyLaunch.positional(new String[] {"--lobby", "https://example.test"}));
    assertArrayEquals(new String[0], OnlineLobbyLaunch.positional(null));
  }

  @Test
  void lobbyWithoutProfileIsAConfigurationErrorNotASilentFallback() {
    assertEquals(Mode.LOBBY, OnlineLobbyLaunch.select(true, true));
    assertThrows(IllegalArgumentException.class, () -> OnlineLobbyLaunch.select(true, false));
  }

  @Test
  void returnFromGameBlocksRejoinUntilTheConservativeLeaseDeadline() {
    long left = 1_000_000L;
    long deadline = OnlineLobbyLaunch.leaseDeadline(left);
    assertEquals(left + OnlineLobbyLaunch.CLIENT_LEASE_HOLD_MILLIS, deadline);
    assertTrue(OnlineLobbyLaunch.CLIENT_LEASE_HOLD_MILLIS > 30_000L, "must exceed the server's 30 s lease");

    var model = new OnlineLobbyModel(KEY, new UUID(0, 1));
    model.beginReturnFromGame();
    model.finishReturnFromGame(deadline);
    assertEquals(
        OnlineLobbyLaunch.CLIENT_LEASE_HOLD_MILLIS, model.millisUntilRejoin(left).getAsLong());
    assertEquals(OnlineLobbyModel.ReturnPhase.LEASE_HELD, model.phase(deadline - 1));
    assertEquals(OnlineLobbyModel.ReturnPhase.IDLE, model.phase(deadline));
  }

  @Test
  void credentialsAndModelNeverExposeTheAccountKey() {
    var model = new OnlineLobbyModel(KEY, null);
    assertFalse(model.toString().contains(KEY));
    assertFalse(new OnlineConnection.Credentials(KEY, new UUID(0, 1)).toString().contains(KEY));
  }
}
