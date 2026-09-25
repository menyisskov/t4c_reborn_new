package com.perso.T4C.item;

import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.List;
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

  /**
   * Pads/trims the per-item durability and charge lists so they line up with the storage list.
   * Saves from before these lists existed load with full durability and full charges.
   */
  public static void synchronize(Player player) {
    if (player == null) return;
    int size = player.getStorage().size();
    List<Double> durability = player.getStorageDurability();
    while (durability.size() < size) durability.add(ItemDurabilityService.MAX);
    while (durability.size() > size) durability.remove(durability.size() - 1);
    for (int i = 0; i < size; i++) {
      Double value = durability.get(i);
      durability.set(
          i,
          value == null
              ? ItemDurabilityService.MAX
              : Math.max(0d, Math.min(ItemDurabilityService.MAX, value)));
    }
    List<Integer> charges = player.getStorageCharges();
    while (charges.size() < size) charges.add(-1);
    while (charges.size() > size) charges.remove(charges.size() - 1);
  }

  /** Durability of the stored item at {@code index}, or full durability if out of range. */
  public static double durability(Player player, int index) {
    synchronize(player);
    return player == null || index < 0 || index >= player.getStorageDurability().size()
        ? ItemDurabilityService.MAX
        : player.getStorageDurability().get(index);
  }

  public static InventoryService.Result deposit(Player player, int inventoryIndex, String itemKey) {
    if (player == null) return InventoryService.Result.failure(InventoryService.Failure.ITEM_NOT_OWNED, itemKey);
    int resolved = inventoryIndex;
    if (resolved < 0
        || resolved >= player.getInventory().size()
        || !Objects.equals(itemKey, player.getInventory().get(resolved))) {
      resolved = player.getInventory().indexOf(itemKey);
    }
    if (resolved < 0) return InventoryService.Result.failure(InventoryService.Failure.ITEM_NOT_OWNED, itemKey);
    double durability = ItemDurabilityService.inventory(player, resolved);
    int charges = InventoryService.chargesForNextInstance(player, itemKey);
    InventoryService.Result removed = InventoryService.remove(player, resolved, itemKey);
    if (!removed.success()) return removed;
    synchronize(player);
    player.getStorage().add(removed.itemKey());
    player.getStorageDurability().add(durability);
    player.getStorageCharges().add(charges);
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
    synchronize(player);
    double durability = player.getStorageDurability().get(resolved);
    int charges = player.getStorageCharges().get(resolved);
    InventoryService.Result added = InventoryService.add(player, itemKey, charges);
    if (!added.success()) return added;
    ItemDurabilityService.synchronize(player);
    List<Double> inventoryDurability = player.getInventoryDurability();
    inventoryDurability.set(inventoryDurability.size() - 1, durability);
    player.getStorage().remove(resolved);
    player.getStorageDurability().remove(resolved);
    player.getStorageCharges().remove(resolved);
    return added;
  }

  /** Deposits up to {@code count} copies of {@code itemKey}, stopping at the first failure. */
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

  /**
   * Withdraws up to {@code count} copies of {@code itemKey}. Stops at the first failure and
   * returns it (or success with the number moved), so the caller can tell the player why the rest
   * stayed behind (too heavy, unique item already owned).
   */
  public static Transfer withdrawMany(Player player, String itemKey, int count) {
    int moved = 0;
    InventoryService.Result last = InventoryService.Result.success(itemKey);
    for (int i = 0; i < count; i++) {
      int index = player.getStorage().indexOf(itemKey);
      if (index < 0) break;
      last = withdraw(player, index, itemKey);
      if (!last.success()) break;
      moved++;
    }
    return new Transfer(moved, last);
  }

  /**
   * Deposits every item in the backpack, optionally limited to one category. Equipped gear is
   * never touched (it isn't in the backpack), so this is safe to use as a one-click "stash loot".
   */
  public static int depositAll(Player player, Category category) {
    if (player == null) return 0;
    int moved = 0;
    for (int i = player.getInventory().size() - 1; i >= 0; i--) {
      String key = player.getInventory().get(i);
      if (key == null || (category != null && categoryOf(key) != category)) continue;
      if (deposit(player, i, key).success()) moved++;
    }
    return moved;
  }

  /** Outcome of a multi-item withdrawal: how many moved and the result that ended it. */
  public record Transfer(int moved, InventoryService.Result lastResult) {}

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
