package com.perso.T4C.item;

import com.perso.T4C.player.Player;

/** Pays only for inventory instances successfully removed by a vendor sale. */
public final class ItemSaleService {
  private ItemSaleService() {}

  public static long sell(Player player, String itemKey, int quantity) {
    if (player == null || quantity <= 0) return 0;
    ItemDefinition item = ItemRegistry.findByKey(itemKey);
    long price = ItemSalePricing.sellPrice(item);
    if (price <= 0) return 0;
    int count = Math.min(quantity, InventoryService.count(player, item.getKey()));
    long before = player.getGold();
    for (int i = 0; i < count; i++) {
      if (!InventoryService.destroyOne(player, item.getKey()).success()) break;
      long room = (long) Integer.MAX_VALUE - player.getGold();
      player.setGold((int) (player.getGold() + Math.min(room, price)));
    }
    return player.getGold() - before;
  }
}
