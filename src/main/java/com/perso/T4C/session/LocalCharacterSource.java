package com.perso.T4C.session;

import com.perso.T4C.helper.LocalCharacterStore;
import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.player.Player;

/**
 * Single-player character source: the unchanged lookup that used to live in the game screen. Reads
 * the active named character's save, or the legacy save file when none is active.
 */
final class LocalCharacterSource implements CharacterSource {
  @Override
  public PlayerStateDto loadInitialState() {
    try {
      LocalCharacterStore.CharacterSlot active = LocalCharacterStore.getActiveCharacter();
      PlayerStateDto state =
          active == null ? PlayerStateStore.load() : LocalCharacterStore.loadState(active);
      if (state != null && active != null) {
        if (state.name == null || state.name.isBlank()) state.name = active.name();
        if (state.gender == null || state.gender.isBlank()) state.gender = active.gender();
      }
      return state;
    } catch (IllegalStateException migrationFailure) {
      throw migrationFailure;
    } catch (Throwable ignored) {
      return null;
    }
  }

  @Override
  public void applyTo(PlayerStateDto state, Player player) {
    PlayerStateStore.applyToPlayer(state, player);
  }
}
