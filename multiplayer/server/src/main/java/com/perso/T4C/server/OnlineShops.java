package com.perso.T4C.server;

import com.perso.T4C.item.ShopRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.NpcShopContent;
import com.perso.T4C.online.model.ShopOffersReply;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Server-owned shop prices and listings built from the bundled vendor catalog. */
final class OnlineShops {
  private static NpcShopCatalog catalog;
  private static Map<String, Long> resale;

  private OnlineShops() {}

  static synchronized NpcShopCatalog catalog() {
    if (catalog == null) {
      NpcShopCatalog loaded;
      try { loaded = NpcShopCatalog.loadBundled(); }
      catch (IOException unavailable) { throw new IllegalStateException("NPC shop content unavailable", unavailable); }
      var prices = new LinkedHashMap<String, Long>();
      for (var vendor : loaded.content().vendors())
        for (var offer : vendor.offers()) prices.putIfAbsent(offer.itemKey(), offer.sellPrice());
      resale = Map.copyOf(prices);
      catalog = loaded;
    }
    return catalog;
  }

  static Optional<NpcShopContent.Vendor> vendor(String npcId) {
    return catalog().content().vendors().stream().filter(v -> v.npcId().equals(npcId)).findFirst();
  }

  /** Buy price of one unit from this vendor, empty when the vendor does not stock the item. */
  static Optional<Long> buyPrice(NpcShopContent.Vendor vendor, String itemKey) {
    return vendor.offers().stream().filter(o -> o.itemKey().equals(itemKey)).findFirst()
        .map(NpcShopContent.Offer::buyPrice);
  }

  /**
   * Per-unit payout when the character sells an item to any shopkeeper. Items that appear in vendor
   * stock use the exported single-player resale price; other items use the base rule shared with
   * single-player. Items whose resale value comes from monster drop tables are not priced here (0).
   */
  static long sellPrice(ItemContent.Definition item) {
    catalog();
    Long exported;
    synchronized (OnlineShops.class) { exported = resale.get(item.key()); }
    if (exported != null) return exported;
    long base = ShopRules.baseSellPrice(item.price(), item.rarityPriced(), item.undroppable());
    return base == ShopRules.NEEDS_DROP_TABLE ? 0L : base;
  }

  /** The listing for one shopkeeper: stock to buy and the character's unequipped goods to sell. */
  static ShopOffersReply offers(long revision, long gold, String npcId, NpcShopContent.Vendor vendor,
      CharacterProgress progress, ItemContent items) {
    var buy = new java.util.ArrayList<ShopOffersReply.Line>();
    for (var offer : vendor.offers()) buy.add(new ShopOffersReply.Line(offer.itemKey(), offer.buyPrice(), 0));
    var worn = new HashSet<>(progress.equipment().values());
    var carried = new LinkedHashMap<String, Integer>();
    for (var item : progress.inventory())
      if (!worn.contains(item.id())) carried.merge(item.key(), item.quantity(), Math::addExact);
    var sell = new java.util.ArrayList<ShopOffersReply.Line>();
    carried.forEach((key, quantity) -> {
      var definition = items.items().get(key);
      long price = definition == null ? 0L : sellPrice(definition);
      if (ShopRules.sellable(price)) sell.add(new ShopOffersReply.Line(key, price, quantity));
    });
    return new ShopOffersReply(revision, gold, npcId, buy, sell);
  }
}
