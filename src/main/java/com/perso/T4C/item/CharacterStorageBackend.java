package com.perso.T4C.item;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.Player;
import java.util.List;

/** {@link StorageBackend} for the character's own personal storage chest. */
public final class CharacterStorageBackend implements StorageBackend {
  private final Player player;

  public CharacterStorageBackend(Player player) {
    this.player = player;
  }

  @Override
  public String label() {
    return I18n.key("ui.personal_storage", "PERSONAL STORAGE");
  }

  @Override
  public List<String> items() {
    return player.getStorage();
  }

  @Override
  public void synchronize() {
    StorageService.synchronize(player);
  }

  @Override
  public double durability(int index) {
    return StorageService.durability(player, index);
  }

  @Override
  public InventoryService.Result deposit(int inventoryIndex, String itemKey) {
    return StorageService.deposit(player, inventoryIndex, itemKey);
  }

  @Override
  public InventoryService.Result withdraw(int storageIndex, String itemKey) {
    return StorageService.withdraw(player, storageIndex, itemKey);
  }

  @Override
  public int depositAll(StorageService.Category category) {
    return StorageService.depositAll(player, category);
  }

  @Override
  public int gold() {
    return player.getStorageGold();
  }

  @Override
  public void depositGold(int amount) {
    StorageService.depositGold(player, amount);
  }

  @Override
  public void withdrawGold(int amount) {
    StorageService.withdrawGold(player, amount);
  }
}
