package com.perso.T4C.session;

import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.player.Player;

/** Where the Player comes from (a save file locally, a server snapshot remotely). */
public interface CharacterSource {
  /** The character state to start from, or {@code null} for a brand-new character. */
  PlayerStateDto loadInitialState();

  /** Copies a state returned by {@link #loadInitialState()} onto the freshly built player. */
  void applyTo(PlayerStateDto state, Player player);
}
