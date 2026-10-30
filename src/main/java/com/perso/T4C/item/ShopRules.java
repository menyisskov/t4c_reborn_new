package com.perso.T4C.item;

import com.perso.T4C.monster.core.MonsterDef;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Vendor arithmetic and rarity pricing shared by single-player (ShopScreen, ItemSaleService) and
 * the online server. Drop indexing reads immutable monster definitions; it has no renderer or save
 * dependencies.
 */
public final class ShopRules {
  /** Largest number of units a single online buy or sell request may name. */
  public static final int MAX_TRADE_QUANTITY = 99;

  /** Single-player gold is an int, so a sale never credits past this ceiling. */
  public static final long GOLD_CEILING = Integer.MAX_VALUE;

  /** Marker returned by {@link #baseSellPrice} when the price depends on monster drop tables. */
  public static final long NEEDS_DROP_TABLE = -1L;
  private static final Set<String> CRAFTING_MATERIAL_KEYS = Set.of("item.wyrmforged_ember",
      "item.veiled_aether_shard", "item.tempered_godcore", "item.bound_godsigil");

  private ShopRules() {}

  /** Cost of {@code quantity} units; overflow throws instead of wrapping. */
  public static long purchaseCost(long unitPrice, int quantity) {
    if (unitPrice < 0 || quantity < 0) throw new IllegalArgumentException("Invalid purchase");
    return Math.multiplyExact(unitPrice, (long) quantity);
  }

  /** A purchase is allowed when the balance covers the whole cost (exactly equal is allowed). */
  public static boolean canAfford(long gold, long cost) {
    return gold >= cost;
  }

  public static long balanceAfterPurchase(long gold, long cost) {
    return gold - cost;
  }

  /**
   * The vendor resale price that does not need monster drop tables, exactly as ItemSalePricing
   * computes it: undroppable items are worth nothing; anything with a shop price (or that is not
   * rarity priced) sells for half its price, at least 1. Returns {@link #NEEDS_DROP_TABLE} for
   * rarity-priced items with no shop price, whose resale value comes from their drop chance.
   */
  public static long baseSellPrice(long price, boolean rarityPriced, boolean undroppable) {
    if (undroppable) return 0L;
    if (price > 0 || !rarityPriced) return Math.max(1L, price / 2L);
    return NEEDS_DROP_TABLE;
  }

  /** Only items worth gold can be sold. */
  public static boolean sellable(long unitSellPrice) {
    return unitSellPrice > 0;
  }

  /** Same drop rarity formula and fallback values used by offline and online vendors. */
  public static long rarityDropPrice(String itemKey, boolean unique, double chance) {
    if (itemKey.startsWith("item.ancient_celestial_") || itemKey.startsWith("item.empyrean_")) {
      if (chance <= 0) return 200_000;
      long dropPrice = priceForDropChance(chance);
      long scaled = dropPrice >= Integer.MAX_VALUE / 50L ? Integer.MAX_VALUE : dropPrice * 50L;
      return Math.max(200_000, scaled);
    }
    if (chance > 0) return priceForDropChance(chance);
    if (itemKey.startsWith("item.godsforged_")) return 100_000;
    if (unique || CRAFTING_MATERIAL_KEYS.contains(itemKey)) return 25_000;
    return 2_500;
  }

  /** A 1% drop sells for 10,000 gold; rarer drops pay proportionally more. */
  public static long priceForDropChance(double chance) {
    if (!Double.isFinite(chance) || chance <= 0 || chance > 1)
      throw new IllegalArgumentException("Drop probability must be finite and in (0,1]");
    return Math.round(100.0 / chance);
  }

  /** Indexes the easiest authored monster drop chance, including repeated independent rolls. */
  public static Map<String, Double> indexDropChances(List<MonsterDef> monsters) {
    Map<String, Double> result = new HashMap<>();
    for (MonsterDef monster : monsters) {
      if (monster == null || monster.getLoot() == null) continue;
      Map<String, Double> misses = new HashMap<>();
      for (MonsterDef.LootDrop drop : monster.getLoot()) {
        if (drop == null || drop.getItem() == null || drop.getItem().isBlank()) continue;
        double chance = drop.getChance();
        if (!Double.isFinite(chance) || chance <= 0) continue;
        misses.merge(normalizeKey(drop.getItem()), 1.0 - Math.min(1.0, chance), (a, b) -> a * b);
      }
      misses.forEach((key, miss) -> result.merge(key, 1.0 - miss, Math::max));
    }
    return Map.copyOf(result);
  }

  public static double dropChance(Map<String, Double> indexed, String itemKey) {
    return indexed.getOrDefault(normalizeKey(itemKey), 0.0);
  }

  private static String normalizeKey(String key) {
    return ItemKeys.normalize(key);
  }

  /**
   * Gold after being paid for one unit: never past {@link #GOLD_CEILING}, the surplus is lost
   * (as in single-player, one unit at a time).
   */
  public static long creditOneUnit(long gold, long unitPrice) {
    long room = Math.max(0L, GOLD_CEILING - gold);
    return gold + Math.min(room, unitPrice);
  }

  /** Gold after selling {@code units} units one at a time. */
  public static long balanceAfterSale(long gold, long unitPrice, int units) {
    long balance = gold;
    for (int i = 0; i < units; i++) balance = creditOneUnit(balance, unitPrice);
    return balance;
  }
}
