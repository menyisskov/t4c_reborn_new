package com.perso.T4C.online.model;

import com.perso.T4C.item.ItemKeys;
import java.util.Objects;
import java.util.UUID;

/**
 * One item rolled by a lethal strike, before it is committed. The ID becomes the inventory
 * instance ID on pickup. Quantity is per-unit for offline parity; stackable drops are not
 * authored yet.
 */
public record LootDropDraft(UUID id, int index, String key, int quantity, int charges, int world,
    double x, double y) {
  public LootDropDraft {
    Objects.requireNonNull(id, "id");
    if (index < 0) throw new IllegalArgumentException("Invalid loot index");
    if (key == null || !key.startsWith("item.") || key.length() <= 5 || key.length() > 256
        || !ItemKeys.normalize(key).equals(key)) {
      throw new IllegalArgumentException("Unnormalized loot item key");
    }
    if (quantity < 1) throw new IllegalArgumentException("Loot quantity must be positive");
    if (charges < 0) throw new IllegalArgumentException("Loot charges must not be negative");
    if (world < 0) throw new IllegalArgumentException("Invalid loot world");
    if (!Double.isFinite(x) || !Double.isFinite(y)) throw new IllegalArgumentException("Invalid loot position");
  }
}
