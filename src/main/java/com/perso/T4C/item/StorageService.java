package com.perso.T4C.item;

import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.Objects;

public final class StorageService {
  public enum Category {
    WEAPONS,
    ARMOR,
    LEGGINGS,
    BOOTS,
    GLOVES,
    RINGS,
    MISC
  }

  private StorageService() {}

  public static Category categoryOf(String itemKey) {
    ItemDefinition definition = ItemRegistry.findByKey(itemKey);
    BodyPart slot = definition == null ? null : definition.getBodyPart();
    if (slot == null) return Category.MISC;
    return switch (slot) {
      case WEAPON, WEAPON2 -> Category.WEAPONS;
      case LEGS -> Category.LEGGINGS;
      case FEET, BOOT -> Category.BOOTS;
      case LEFT_HAND, RIGHT_HAND -> Category.GLOVES;
      case RING1, RING2 -> Category.RINGS;
      case BODY,
          HEAD,
          BELT,
          NECK,
          BRACER,
          BACK,
          SHIELD,
          ROBELEGS,
          HAIR,
          HAT,
          MASK,
          CAPE,
          LEFT_ARM,
          RIGHT_ARM ->
          Category.ARMOR;
    };
  }

  public static InventoryService.Result deposit(Player player, int inventoryIndex, String itemKey) {
    if (player == null) return InventoryService.Result.failure(InventoryService.Failure.ITEM_NOT_OWNED, itemKey);
    InventoryService.Result removed = InventoryService.remove(player, inventoryIndex, itemKey);
    if (!removed.success()) return removed;
    player.getStorage().add(removed.itemKey());
    return removed;
  }

  public static InventoryService.Result withdraw(Player player, int storageIndex, String itemKey) {
    if (player == null || player.getStorage().isEmpty()) {
      return InventoryService.Result.failure(InventoryService.Failure.INVENTORY_EMPTY, itemKey);
    }
    int resolved = storageIndex;
    if (resolved < 0
        || resolved >= player.getStorage().size()
        || !Objects.equals(itemKey, player.getStorage().get(resolved))) {
      resolved = player.getStorage().indexOf(itemKey);
    }
    if (resolved < 0) return InventoryService.Result.failure(InventoryService.Failure.ITEM_NOT_OWNED, itemKey);
    InventoryService.Result added = InventoryService.add(player, itemKey);
    if (!added.success()) return added;
    player.getStorage().remove(resolved);
    return added;
  }

  public static int depositMany(Player player, String itemKey, int count) {
    int moved = 0;
    for (int i = 0; i < count; i++) {
      int index = player.getInventory().indexOf(itemKey);
      if (index < 0) break;
      if (!deposit(player, index, itemKey).success()) break;
      moved++;
    }
    return moved;
  }

  public static int withdrawMany(Player player, String itemKey, int count) {
    int moved = 0;
    for (int i = 0; i < count; i++) {
      int index = player.getStorage().indexOf(itemKey);
      if (index < 0) break;
      if (!withdraw(player, index, itemKey).success()) break;
      moved++;
    }
    return moved;
  }

  public static void depositGold(Player player, int amount) {
    if (player == null || amount <= 0) return;
    int actual = Math.min(amount, player.getGold());
    if (actual <= 0) return;
    player.setGold(player.getGold() - actual);
    player.setStorageGold(player.getStorageGold() + actual);
  }

  public static void withdrawGold(Player player, int amount) {
    if (player == null || amount <= 0) return;
    int actual = Math.min(amount, player.getStorageGold());
    if (actual <= 0) return;
    player.setStorageGold(player.getStorageGold() - actual);
    player.addGold(actual);
  }
}
