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
 * carried items, and number keys 1-9 address rows on the current page. Unknown slots are dropped.
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

  /** Clamp after a server refresh removes items, including when the list becomes empty. */
  static int clampPage(int page, int size) {
    return Math.max(0, Math.min(page, Math.max(0, (size - 1) / MAX_CHOICES)));
  }

  static List<Row> page(List<Row> rows, int page) {
    int start = clampPage(page, rows.size()) * MAX_CHOICES;
    return rows.subList(start, Math.min(start + MAX_CHOICES, rows.size()));
  }

  /** Digit 1 selects the first row of the current, clamped page. */
  static Optional<Row> choose(List<Row> rows, List<Row> displayed, int page, int displayedPage, int digit) {
    if (page != displayedPage || !rows.equals(displayed)) return Optional.empty();
    var visible = page(rows, page);
    if (digit < 1 || digit > visible.size() || digit > MAX_CHOICES) return Optional.empty();
    return Optional.of(visible.get(digit - 1));
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
