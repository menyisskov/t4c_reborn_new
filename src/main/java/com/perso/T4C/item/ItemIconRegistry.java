package com.perso.T4C.item;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.ItemIconBinaryIO;
import java.io.File;
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
    return def == null ? null : iconFor(def.getAppearanceId());
  }

  public static synchronized void invalidate() {
    byAppearanceId = null;
  }

  private static void load() {
    if (byAppearanceId != null) {
      return;
    }
    Map<Integer, String> map = Map.of();
    File file = new File(Paths.ITEM_ICONS_BIN);
    if (file.exists()) {
      try {
        map = ItemIconBinaryIO.read(file);
      } catch (Exception ignored) {
      }
    }
    byAppearanceId = Map.copyOf(map);
  }
}
