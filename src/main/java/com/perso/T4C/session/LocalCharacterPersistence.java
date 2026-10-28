package com.perso.T4C.session;

import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.player.Player;

/** Single-player persistence: the unchanged {@link PlayerStateStore} save file. */
final class LocalCharacterPersistence implements CharacterPersistence {
  @Override
  public void save(Player player, float dayNightHour) {
    PlayerStateStore.save(player, dayNightHour);
  }
}
