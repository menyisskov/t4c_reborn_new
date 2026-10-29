package com.perso.T4C.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class RemoteGameSessionTest {
  @Test
  void isARemoteSessionWithItsOwnCharacterSourceAndNoPersistence() {
    RemoteGameSession session =
        new RemoteGameSession(() -> ProgressMirrorTest.view(ProgressMirrorTest.fixture()));
    assertEquals(SessionKind.REMOTE, session.kind());
    assertInstanceOf(RemoteCharacterSource.class, session.characters());
    assertInstanceOf(RemoteCharacterPersistence.class, session.persistence());
    assertNull(session.mirrored(), "nothing is read before the screen asks");

    PlayerStateDto state = session.characters().loadInitialState();

    assertNotNull(state);
    assertEquals("Aelwyn", state.name);
    assertNotNull(session.mirrored());
    Player player = new Player();
    session.characters().applyTo(state, player);
    assertTrue(player.isRemote());
  }

  @Test
  void aViewThatIsNotReadyFailsInsteadOfStartingANewCharacter() {
    RemoteGameSession session = new RemoteGameSession(() -> null);
    assertThrows(IllegalStateException.class, () -> session.characters().loadInitialState());
  }

  @Test
  void theWorldIsTheSnapshotDriverAndCommandsAreTheRemoteIntents() {
    RemoteGameSession session = new RemoteGameSession(() -> null);
    assertInstanceOf(RemoteWorldDriver.class, session.world());
    assertInstanceOf(RemotePlayerCommands.class, session.commands());
    session.commands().attack(null);
    assertThrows(
        IllegalStateException.class,
        () -> session.commands().move(1, 0, 0.016f),
        "used before the screen bound it");
  }
}
