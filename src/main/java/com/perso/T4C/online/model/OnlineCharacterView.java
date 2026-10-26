package com.perso.T4C.online.model;

import java.util.Objects;
import java.util.UUID;

/** Owner-only committed state. No account ID, access key, session token or database lease. */
public record OnlineCharacterView(
    UUID id, String name, long gold, long experience, long revision, CharacterProgress progress) {
  public OnlineCharacterView {
    Objects.requireNonNull(id);
    if (name == null || name.isBlank() || gold < 0 || experience < 0 || revision < 0)
      throw new IllegalArgumentException("Invalid online character");
    // Legacy movement-only profiles deliberately retain null progress until explicitly migrated.
  }
}
