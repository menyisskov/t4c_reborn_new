package com.perso.T4C.online.model;

import com.perso.T4C.item.ItemKeys;
import java.util.Objects;
import java.util.UUID;

/**
 * Public view of an item lying on the ground in a region. The owner is only a public player ID,
 * never a character or account ID. {@code ownerPlayerId} is named only for the viewer's own owner
 * window; another owner's window is reported with a null ID and its approximate remaining time.
 */
public record GroundLootView(UUID id, double x, double y, String key, String ownerPlayerId,
    long ownerRemainingMs) {
  public GroundLootView {
    Objects.requireNonNull(id, "id");
    if (!Double.isFinite(x) || !Double.isFinite(y)) throw new IllegalArgumentException("Invalid loot position");
    if (key == null || !key.startsWith("item.") || key.length() <= 5 || key.length() > 256
        || !ItemKeys.normalize(key).equals(key)) {
      throw new IllegalArgumentException("Unnormalized loot item key");
    }
    if (ownerRemainingMs < 0) throw new IllegalArgumentException("Owner window must not be negative");
  }
}
