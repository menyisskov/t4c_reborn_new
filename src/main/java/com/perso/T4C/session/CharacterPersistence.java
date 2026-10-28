package com.perso.T4C.session;

import com.perso.T4C.player.Player;

/** How a session stores its character: a save file locally, deliberately nothing remotely. */
public interface CharacterPersistence {
  /**
   * Stores {@code player}; {@code dayNightHour} is saved with it (the clock belongs to the save).
   */
  void save(Player player, float dayNightHour);

  /** Stores {@code player} after a window or input action, keeping the last saved clock. */
  void save(Player player);

  /** The single-player persistence: the unchanged local save file. */
  static CharacterPersistence local() {
    return new LocalCharacterPersistence();
  }
}
