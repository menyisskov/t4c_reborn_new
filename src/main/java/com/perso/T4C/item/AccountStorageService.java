package com.perso.T4C.item;

import com.perso.T4C.player.Player;
import java.util.List;
import java.util.Objects;

/**
 * Deposit/withdraw logic for the shared account vault ({@link AccountStorage}). Mirrors {@link
 * StorageService} item-for-item, but the "stash" side is the account-wide vault instead of the
 * character's own storage list, so gear and gold can move between a player's own characters.
 */
public final class AccountStorageService {
  private AccountStorageService() {}

  /** Pads/trims the vault's durability and charge lists so they line up with its item list. */
  public static void synchronize() {
    AccountStorage account = AccountStorage.get();
    int size = account.storage().size();
    List<Double> durability = account.storageDurability();
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
    List<Integer> charges = account.storageCharges();
    while (charges.size() < size) charges.add(-1);
    while (charges.size() > size) charges.remove(charges.size() - 1);
  }

  public static double durability(int index) {
    synchronize();
    AccountStorage account = AccountStorage.get();
    return index < 0 || index >= account.storageDurability().size()
        ? ItemDurabilityService.MAX
        : account.storageDurability().get(index);
  }

  public static InventoryService.Result deposit(Player player, int inventoryIndex, String itemKey) {
    if (player == null) {
      return InventoryService.Result.failure(InventoryService.Failure.ITEM_NOT_OWNED, itemKey);
    }
    int resolved = inventoryIndex;
    if (resolved < 0
        || resolved >= player.getInventory().size()
        || !Objects.equals(itemKey, player.getInventory().get(resolved))) {
      resolved = player.getInventory().indexOf(itemKey);
    }
    if (resolved < 0) {
      return InventoryService.Result.failure(InventoryService.Failure.ITEM_NOT_OWNED, itemKey);
    }
    double durability = ItemDurabilityService.inventory(player, resolved);
    int charges = InventoryService.chargesForNextInstance(player, itemKey);
    InventoryService.Result removed = InventoryService.remove(player, resolved, itemKey);
    if (!removed.success()) return removed;
    synchronize();
    AccountStorage account = AccountStorage.get();
    account.storage().add(removed.itemKey());
    account.storageDurability().add(durability);
    account.storageCharges().add(charges);
    account.save();
    return removed;
  }

  public static InventoryService.Result withdraw(Player player, int storageIndex, String itemKey) {
    AccountStorage account = AccountStorage.get();
    if (player == null || account.storage().isEmpty()) {
      return InventoryService.Result.failure(InventoryService.Failure.INVENTORY_EMPTY, itemKey);
    }
    int resolved = storageIndex;
    if (resolved < 0
        || resolved >= account.storage().size()
        || !Objects.equals(itemKey, account.storage().get(resolved))) {
      resolved = account.storage().indexOf(itemKey);
    }
    if (resolved < 0) {
      return InventoryService.Result.failure(InventoryService.Failure.ITEM_NOT_OWNED, itemKey);
    }
    synchronize();
    double durability = account.storageDurability().get(resolved);
    int charges = account.storageCharges().get(resolved);
    InventoryService.Result added = InventoryService.add(player, itemKey, charges);
    if (!added.success()) return added;
    ItemDurabilityService.synchronize(player);
    List<Double> inventoryDurability = player.getInventoryDurability();
    inventoryDurability.set(inventoryDurability.size() - 1, durability);
    account.storage().remove(resolved);
    account.storageDurability().remove(resolved);
    account.storageCharges().remove(resolved);
    account.save();
    return added;
  }

  /**
   * Deposits every item in the backpack, optionally limited to one category. Equipped gear is
   * never touched (it isn't in the backpack), so this is safe to use as a one-click "stash loot".
   */
  public static int depositAll(Player player, StorageService.Category category) {
    if (player == null) return 0;
    int moved = 0;
    for (int i = player.getInventory().size() - 1; i >= 0; i--) {
      String key = player.getInventory().get(i);
      if (key == null || (category != null && StorageService.categoryOf(key) != category)) continue;
      if (deposit(player, i, key).success()) moved++;
    }
    return moved;
  }

  public static void depositGold(Player player, int amount) {
    if (player == null || amount <= 0) return;
    int actual = Math.min(amount, player.getGold());
    if (actual <= 0) return;
    player.setGold(player.getGold() - actual);
    AccountStorage account = AccountStorage.get();
    account.setStorageGold(account.storageGold() + actual);
    account.save();
  }

  public static void withdrawGold(Player player, int amount) {
    if (player == null || amount <= 0) return;
    AccountStorage account = AccountStorage.get();
    int actual = Math.min(amount, account.storageGold());
    if (actual <= 0) return;
    account.setStorageGold(account.storageGold() - actual);
    player.addGold(actual);
    account.save();
  }
}
