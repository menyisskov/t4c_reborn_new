package com.perso.T4C.helper;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.Map;

public final class PlayerAppearanceDefaults {
  private static final String MITHRIL_PREFIX = "PupMithrilPlate";
  private static final String PLATE_PREFIX = "PupPlate";

  private PlayerAppearanceDefaults() {}

  public static void applyDefaults(Player player) {
    if (player == null || player.getAnimations() == null) {
      return;
    }
    Map<BodyPart, String> partMap = player.getAnimations().getPartMap();
    if (partMap == null) {
      return;
    }
    Map<BodyPart, String> nakedParts = AppearanceDefaultsCatalog.nakedParts(player.getGender());
    resetEquipmentVisuals(partMap);
    for (Map.Entry<BodyPart, String> entry : nakedParts.entrySet()) {
      ensureDefaultPart(partMap, entry.getKey(), entry.getValue());
    }
    applyEquippedAppearances(player, partMap);
    ConcealmentResolver.applyConcealment(partMap);
    player.getAnimations().setBowEquipped(hasBowEquipped(player));
  }

  public static boolean hasBowEquipped(Player player) {
    if (player == null || player.getEquippedItems() == null) {
      return false;
    }
    for (String itemName : player.getEquippedItems().values()) {
      if (itemName == null || itemName.isEmpty()) {
        continue;
      }
      ItemDefinition def = ItemDefinition.get(itemName);
      if (def != null && def.isBow()) {
        return true;
      }
    }
    return false;
  }

  public static String getAppearanceFor(String itemName, BodyPart part) {
    if (itemName == null || itemName.isEmpty()) {
      return null;
    }
    ItemDefinition def = ItemDefinition.get(itemName);
    if (def == null) {
      return itemName;
    }
    String appearance = def.getAppearanceEquippedFor(part);
    if (appearance != null && !appearance.isEmpty()) {
      return availableAppearance(appearance);
    }
    return null;
  }

  private static String availableAppearance(String appearance) {
    if (appearance == null) return null;
    return appearance.startsWith(MITHRIL_PREFIX)
        ? PLATE_PREFIX + appearance.substring(MITHRIL_PREFIX.length())
        : appearance;
  }

  private static void applyEquippedAppearances(Player player, Map<BodyPart, String> partMap) {
    if (player.getEquippedItems() == null) {
      return;
    }
    for (Map.Entry<BodyPart, String> entry : player.getEquippedItems().entrySet()) {
      BodyPart part = entry.getKey();
      String itemName = entry.getValue();
      if (part == null || itemName == null || itemName.isEmpty()) {
        continue;
      }
      ItemDefinition def = ItemDefinition.get(itemName);
      String primary = getAppearanceFor(itemName, part);
      BodyPart visualPart = part == BodyPart.HEAD ? BodyPart.HAT : part;
      AppearanceDefaultsCatalog.EquippedAppearance resolved =
          AppearanceDefaultsCatalog.equippedAppearance(player.getGender(), visualPart, primary);
      primary = resolved.sprite();
      visualPart = resolved.bodyPart();
      if (primary != null && !primary.isEmpty()) {
        partMap.put(visualPart, primary);
      }
      if (def != null && def.getSecondaryBodyPart() != null) {
        String secondary = availableAppearance(def.getAppearanceEquippedSecondary());
        if (secondary != null && !secondary.isEmpty()) {
          AppearanceDefaultsCatalog.EquippedAppearance resolvedSecondary =
              AppearanceDefaultsCatalog.equippedAppearance(
                  player.getGender(), def.getSecondaryBodyPart(), secondary);
          partMap.put(resolvedSecondary.bodyPart(), resolvedSecondary.sprite());
        }
      }
    }
  }

  private static void ensureDefaultPart(
      Map<BodyPart, String> partMap, BodyPart part, String sprite) {
    if (partMap.containsKey(part) || sprite == null || sprite.isEmpty()) {
      return;
    }
    partMap.put(part, sprite);
  }

  private static void resetEquipmentVisuals(Map<BodyPart, String> partMap) {
    for (BodyPart part : BodyPart.values()) {
      if (part != BodyPart.HAIR) partMap.remove(part);
    }
  }
}
