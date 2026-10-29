package com.perso.T4C.online.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * What other players may see of a character: derived on the server from committed state only.
 * Nothing here is client supplied, and nothing private is included (no inventory, stats, gold or
 * worn items that draw nothing).
 */
public final class PublicLook {
  /** Most worn slots a snapshot can carry (one per body part). */
  public static final int MAX_SLOTS = 32;

  private PublicLook() {}

  /**
   * The worn items other players can see: worn slot name to item key, limited to items whose
   * definition draws something on the character. Unknown definitions are left out.
   */
  public static Map<String, String> visibleEquipment(CharacterProgress progress, ItemContent items) {
    if (progress == null || items == null) return Map.of();
    Map<UUID, String> keyById = new HashMap<>();
    for (CharacterProgress.Item item : progress.inventory()) keyById.put(item.id(), item.key());
    Map<String, String> visible = new TreeMap<>();
    for (Map.Entry<String, UUID> worn : progress.equipment().entrySet()) {
      String key = keyById.get(worn.getValue());
      if (key == null) continue;
      ItemContent.Definition definition = items.items().get(key);
      if (definition == null) continue;
      if (blank(definition.appearanceEquippedPrimary()) && blank(definition.appearanceEquippedSecondary())) continue;
      visible.put(worn.getKey(), key);
    }
    return Collections.unmodifiableMap(visible);
  }

  private static boolean blank(String text) {
    return text == null || text.isBlank();
  }
}
