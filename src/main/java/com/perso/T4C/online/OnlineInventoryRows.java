package com.perso.T4C.online;

import com.perso.T4C.online.model.CharacterProgress.Item;
import com.perso.T4C.player.BodyPart;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Inventory panel rows from the committed state. Equipped rows come first (in slot order), then
 * carried items, and number keys 1-9 address rows in that order. Unknown slots are dropped.
 */
final class OnlineInventoryRows {
  /** Number keys 1-9 are the only row choices the panel offers. */
  static final int MAX_CHOICES = 9;

  enum Kind { CARRIED, EQUIPPED }

  /** {@code slot} is set only for equipped rows; {@code instance} identifies the item for equip. */
  record Row(Kind kind, UUID instance, String slot, String itemKey) {}

  private OnlineInventoryRows() {}

  static List<Row> of(List<Item> inventory, Map<String, UUID> equipment) {
    var byId = new HashMap<UUID, Item>();
    inventory.forEach(item -> byId.put(item.id(), item));
    var worn = new HashSet<UUID>();
    var rows = new ArrayList<Row>();
    for (var slot : new TreeMap<>(equipment).entrySet()) {
      var item = byId.get(slot.getValue());
      if (item == null || !validSlot(slot.getKey())) continue;
      worn.add(item.id());
      rows.add(new Row(Kind.EQUIPPED, item.id(), slot.getKey(), item.key()));
    }
    for (var item : inventory) {
      if (!worn.contains(item.id())) rows.add(new Row(Kind.CARRIED, item.id(), null, item.key()));
    }
    return List.copyOf(rows);
  }

  /** Digit 1 is the first row. Only the first nine rows are reachable from the panel. */
  static Optional<Row> choose(List<Row> rows, int digit) {
    if (digit < 1 || digit > rows.size() || digit > MAX_CHOICES) return Optional.empty();
    return Optional.of(rows.get(digit - 1));
  }

  private static boolean validSlot(String slot) {
    try {
      BodyPart.valueOf(slot);
      return true;
    } catch (IllegalArgumentException | NullPointerException unknown) {
      return false;
    }
  }
}
