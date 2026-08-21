package com.perso.T4C.monster.core;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;

public final class MonsterPuppetDress {
  private MonsterPuppetDress() {}

  public static ItemDefinition find(int appearanceGroupId) {
    if (appearanceGroupId <= 0) {
      return null;
    }
    ItemDefinition fallback = null;
    for (ItemDefinition item : ItemRegistry.load()) {
      if (item == null || !hasEquippedVisual(item)) {
        continue;
      }
      if (item.getAppearanceId() == appearanceGroupId) {
        return item;
      }
      if (fallback == null && item.getNumId() == appearanceGroupId) {
        fallback = item;
      }
    }
    return fallback;
  }

  public static boolean hasEquippedVisual(ItemDefinition item) {
    return item.getBodyPart() != null
        && item.getAppearanceEquippedPrimary() != null
        && !item.getAppearanceEquippedPrimary().isBlank();
  }

  public static void addPart(java.util.List<Object> parts, BodyPart bodyPart, String sprite) {
    if (bodyPart == null || sprite == null || sprite.isBlank()) {
      return;
    }
    parts.add(bodyPart);
    parts.add(sprite);
  }
}
