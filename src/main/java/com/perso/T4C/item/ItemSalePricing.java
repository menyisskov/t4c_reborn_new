package com.perso.T4C.item;

import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import java.util.List;
import java.util.Map;

/** Vendor resale is independent of the buy price (zero still means drop/quest-only). */
public final class ItemSalePricing {
  private static List<MonsterDef> cachedMonsters;
  private static Map<String, Double> easiestDrops = Map.of();

  private ItemSalePricing() {}

  public static long sellPrice(ItemDefinition item) {
    if (item == null) return 0;
    long base = ShopRules.baseSellPrice(item.getPrice(), item.isRarityPriced(), item.isUndroppable());
    if (base != ShopRules.NEEDS_DROP_TABLE) return base;
    return ShopRules.rarityDropPrice(item.getKey(), item.isUnique(), easiestDropChance(item.getKey()));
  }

  /** A 1% drop sells for 10,000 gold; rarer drops pay proportionally more. */
  public static long priceForDropChance(double chance) {
    return ShopRules.priceForDropChance(chance);
  }

  public static synchronized double easiestDropChance(String itemKey) {
    List<MonsterDef> monsters = MonsterRegistry.load();
    if (monsters != cachedMonsters) {
      easiestDrops = ShopRules.indexDropChances(monsters);
      cachedMonsters = monsters;
    }
    return ShopRules.dropChance(easiestDrops, itemKey);
  }
}
