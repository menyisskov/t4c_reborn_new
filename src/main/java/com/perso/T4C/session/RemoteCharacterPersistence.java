package com.perso.T4C.session;

import com.perso.T4C.player.Player;

/**
 * Online persistence: deliberately nothing. The server owns the character, so no save call, from
 * the autosave or from any window, may write a byte to this machine's single-player save
 * directory. (Client preferences such as the quick bar are a separate store, added later.)
 */
final class RemoteCharacterPersistence implements CharacterPersistence {
  @Override
  public void save(Player player, float dayNightHour) {}

  @Override
  public void save(Player player) {}
}
