package com.perso.T4C.item;

import com.perso.T4C.mapping.definition.ItemIconDefinitions;
import java.util.Map;

public final class ItemIconRegistry {
  private static Map<Integer, String> byAppearanceId;

  private ItemIconRegistry() {}

  public static synchronized String iconFor(int appearanceId) {
    if (appearanceId <= 0) {
      return null;
    }
    load();
    return byAppearanceId.get(appearanceId);
  }

  public static String iconFor(ItemDefinition def) {
    if (def == null) return null;
    String icon = iconFor(def.getAppearanceId());
    return icon != null ? icon : def.getAppearanceInventory();
  }

  public static synchronized void invalidate() {
    byAppearanceId = null;
  }

  private static void load() {
    if (byAppearanceId != null) {
      return;
    }
    byAppearanceId = ItemIconDefinitions.all();
  }
}
