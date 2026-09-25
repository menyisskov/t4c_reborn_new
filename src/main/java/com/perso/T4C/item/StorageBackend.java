package com.perso.T4C.item;

import java.util.List;

/**
 * Where a storage screen's stash pane reads and writes: either the character's own storage
 * ({@link StorageService}, backed by {@code Player}) or the shared account vault ({@link
 * AccountStorageService}, backed by {@link AccountStorage}). Lets {@code StorageScreen} render
 * and operate on either one without knowing which it's looking at.
 */
public interface StorageBackend {
  /** Window title for this stash, already resolved through i18n. */
  String label();

  /** The stash's item list, in storage order (parallel to durability/charges). */
  List<String> items();

  /** Pads/trims the durability and charge lists so they line up with {@link #items()}. */
  void synchronize();

  double durability(int index);

  InventoryService.Result deposit(int inventoryIndex, String itemKey);

  InventoryService.Result withdraw(int storageIndex, String itemKey);

  int depositAll(StorageService.Category category);

  int gold();

  void depositGold(int amount);

  void withdrawGold(int amount);
}
