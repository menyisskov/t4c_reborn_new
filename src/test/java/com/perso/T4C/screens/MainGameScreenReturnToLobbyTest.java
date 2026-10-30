package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MainGameScreenReturnToLobbyTest {
  @Test
  void permitsReturnOnlyForLobbyLaunchWithStableRemoteStatusAndNoQueuedReturn() {
    assertTrue(MainGameScreen.shouldQueueReturnToLobby(true, "connected", false));
    assertTrue(MainGameScreen.shouldQueueReturnToLobby(true, "disconnected", false));

    assertFalse(MainGameScreen.shouldQueueReturnToLobby(true, "connecting", false));
    assertFalse(MainGameScreen.shouldQueueReturnToLobby(true, "reconnecting", false));
    assertFalse(MainGameScreen.shouldQueueReturnToLobby(true, "closed", false));
    assertFalse(MainGameScreen.shouldQueueReturnToLobby(false, "connected", false));
    assertFalse(MainGameScreen.shouldQueueReturnToLobby(false, "disconnected", false));
    assertFalse(MainGameScreen.shouldQueueReturnToLobby(true, "connected", true));
    assertFalse(MainGameScreen.shouldQueueReturnToLobby(true, "disconnected", true));
  }
}
