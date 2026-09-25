package com.perso.T4C.item;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import java.util.List;

/**
 * {@link StorageBackend} for the shared account vault ({@link AccountStorage}): the same stash
 * on every character on this local roster, so gear and gold can move between a player's own
 * characters without a mule run.
 */
public final class AccountStorageBackend implements StorageBackend {
  private final Player player;

  public AccountStorageBackend(Player player) {
    this.player = player;
  }

  @Override
  public String label() {
    return I18n.key("ui.bloodline_vault", "BLOODLINE VAULT");
  }

  @Override
  public List<String> items() {
    return AccountStorage.get().storage();
  }

  @Override
  public void synchronize() {
    AccountStorageService.synchronize();
  }

  @Override
  public double durability(int index) {
    return AccountStorageService.durability(index);
  }

  @Override
  public InventoryService.Result deposit(int inventoryIndex, String itemKey) {
    return AccountStorageService.deposit(player, inventoryIndex, itemKey);
  }

  @Override
  public InventoryService.Result withdraw(int storageIndex, String itemKey) {
    return AccountStorageService.withdraw(player, storageIndex, itemKey);
  }

  @Override
  public int depositAll(StorageService.Category category) {
    return AccountStorageService.depositAll(player, category);
  }

  @Override
  public int gold() {
    return AccountStorage.get().storageGold();
  }

  @Override
  public void depositGold(int amount) {
    AccountStorageService.depositGold(player, amount);
  }

  @Override
  public void withdrawGold(int amount) {
    AccountStorageService.withdrawGold(player, amount);
  }
}
