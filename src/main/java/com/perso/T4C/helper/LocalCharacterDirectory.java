package com.perso.T4C.helper;

import com.perso.T4C.input.CharacterDirectory;
import com.perso.T4C.player.GmRank;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** {@link CharacterDirectory} over the local character roster and its save files. */
public final class LocalCharacterDirectory implements CharacterDirectory {

  @Override
  public String findName(String name) {
    LocalCharacterStore.CharacterSlot slot = find(name);
    return slot == null ? null : slot.name();
  }

  @Override
  public GmRank storedRank(String name) {
    LocalCharacterStore.CharacterSlot slot = find(name);
    if (slot == null) return null;
    PlayerStateDto state = LocalCharacterStore.loadState(slot);
    return GmRank.fromSave(state == null ? null : state.gmRank);
  }

  @Override
  public boolean setStoredRank(String name, GmRank rank) {
    LocalCharacterStore.CharacterSlot slot = find(name);
    if (slot == null) return false;
    PlayerStateDto state = LocalCharacterStore.loadState(slot);
    if (state == null) return false;
    state.gmRank = (rank == null ? GmRank.PLAYER : rank).name();
    PlayerStateStore.save(slot.stateFile(), state);
    return true;
  }

  @Override
  public Map<String, GmRank> storedRanks() {
    Map<String, GmRank> ranks = new LinkedHashMap<>();
    for (LocalCharacterStore.CharacterSlot slot : slots()) {
      PlayerStateDto state = LocalCharacterStore.loadState(slot);
      ranks.put(slot.name(), GmRank.fromSave(state == null ? null : state.gmRank));
    }
    return ranks;
  }

  private static LocalCharacterStore.CharacterSlot find(String name) {
    if (name == null || name.isBlank()) return null;
    for (LocalCharacterStore.CharacterSlot slot : slots()) {
      if (slot.name().equalsIgnoreCase(name.trim())) return slot;
    }
    return null;
  }

  private static java.util.List<LocalCharacterStore.CharacterSlot> slots() {
    try {
      return LocalCharacterStore.list();
    } catch (IOException e) {
      return java.util.List.of();
    }
  }
}
