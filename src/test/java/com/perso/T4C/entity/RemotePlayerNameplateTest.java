package com.perso.T4C.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class RemotePlayerNameplateTest {
  @Test
  void aNamedPlayerShowsItsNameOnlyWhileHovered() {
    Player other = new Player();
    other.setName("Brindle");
    assertEquals("Brindle", RemotePlayerNameplate.label(other, true));
    assertNull(RemotePlayerNameplate.label(other, false));
  }

  @Test
  void aPlayerWithoutAServerNameShowsNothing() {
    assertNull(RemotePlayerNameplate.label(new Player(), true));
    assertNull(RemotePlayerNameplate.label(null, true));
  }
}
