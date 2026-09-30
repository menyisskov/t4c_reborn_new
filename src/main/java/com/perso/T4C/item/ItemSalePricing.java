package com.perso.T4C.item;

import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Vendor resale is independent of the buy price (zero still means drop/quest-only). */
public final class ItemSalePricing {
  private static final long ENDGAME_MINIMUM = 200_000;
  private static final long ENDGAME_DROP_MULTIPLIER = 50;
  private static final Set<String> CRAFTING_MATERIAL_KEYS =
      Set.of(
          "item.wyrmforged_ember", "item.veiled_aether_shard",
          "item.tempered_godcore", "item.bound_godsigil");
  private static List<MonsterDef> cachedMonsters;
  private static Map<String, Double> easiestDrops = Map.of();

  private ItemSalePricing() {}

  public static long sellPrice(ItemDefinition item) {
    if (item == null || item.isUndroppable()) return 0;
    // Shop stock retains the existing buy/sell spread, preventing buy-and-resell profit.
    if (item.getPrice() > 0 || !item.isRarityPriced()) return Math.max(1L, item.getPrice() / 2L);
    double chance = easiestDropChance(item.getKey());
    if (item.getKey().startsWith("item.ancient_celestial_")
        || item.getKey().startsWith("item.empyrean_")) {
      if (chance <= 0) return ENDGAME_MINIMUM;
      long dropPrice = priceForDropChance(chance);
      long scaled =
          dropPrice >= Integer.MAX_VALUE / ENDGAME_DROP_MULTIPLIER
              ? Integer.MAX_VALUE
              : dropPrice * ENDGAME_DROP_MULTIPLIER;
      return Math.max(ENDGAME_MINIMUM, scaled);
    }
    if (chance > 0) return priceForDropChance(chance);
    if (item.getKey().startsWith("item.godsforged_")) return 100_000;
    if (item.isUnique() || CRAFTING_MATERIAL_KEYS.contains(item.getKey())) return 25_000;
    return 2_500;
  }

  /** A 1% drop sells for 10,000 gold; rarer drops pay proportionally more. */
  public static long priceForDropChance(double chance) {
    if (!Double.isFinite(chance) || chance <= 0 || chance > 1)
      throw new IllegalArgumentException("Drop probability must be finite and in (0,1]");
    return Math.round(100.0 / chance);
  }

  public static synchronized double easiestDropChance(String itemKey) {
    List<MonsterDef> monsters = MonsterRegistry.load();
    if (monsters != cachedMonsters) {
      easiestDrops = indexDropChances(monsters);
      cachedMonsters = monsters;
    }
    return easiestDrops.getOrDefault(ItemDefinition.normalizeKey(itemKey), 0.0);
  }

  static Map<String, Double> indexDropChances(List<MonsterDef> monsters) {
    Map<String, Double> result = new HashMap<>();
    for (MonsterDef monster : monsters) {
      if (monster == null || monster.getLoot() == null) continue;
      Map<String, Double> misses = new HashMap<>();
      for (MonsterDef.LootDrop drop : monster.getLoot()) {
        if (drop == null || drop.getItem() == null || drop.getItem().isBlank()) continue;
        double chance = drop.getChance();
        if (!Double.isFinite(chance) || chance <= 0) continue;
        // Repeated independent rolls for one item make it easier to obtain from this monster.
        misses.merge(
            ItemDefinition.normalizeKey(drop.getItem()),
            1.0 - Math.min(1.0, chance),
            (a, b) -> a * b);
      }
      misses.forEach((key, miss) -> result.merge(key, 1.0 - miss, Math::max));
    }
    return Map.copyOf(result);
  }
}
