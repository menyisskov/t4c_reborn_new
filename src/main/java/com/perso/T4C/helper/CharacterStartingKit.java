package com.perso.T4C.helper;

import java.util.List;
import java.util.Map;

/** Shared creation data; no renderer, registry initialization or local-save access. */
public final class CharacterStartingKit {
  private CharacterStartingKit() {}

  public static final int MIN_GOLD = 201;
  public static final int MAX_GOLD = 250;
  public static final int GOOD_KARMA = 100;
  public static final int EVIL_KARMA = -100;
  public static final String TORCH = "item.torch";
  public static final int TORCH_COUNT = 3;
  public static final int POTION_COUNT = 3;
  public static final List<String> POTIONS =
      List.of("item.light_healing_potion", "item.potion_of_mana");
  public static final Map<String, Integer> SKILLS = Map.of("attack", 15, "dodge", 15, "archery", 15);

  /**
   * Every item a new character of this class starts with, as item key to count: the worn equipment, the class's stacked
   * kit items, the torches and the potions. Offline creation and the online server both build their kit from these same
   * inputs; tests compare each of them with this one definition so the two can never drift apart.
   */
  public static Map<String, Integer> itemCounts(CharacterClass type) {
    Map<String, Integer> counts = new java.util.TreeMap<>();
    type.startingEquipment().values().forEach(key -> counts.merge(key, 1, Integer::sum));
    for (String key : type.startingStacks()) counts.merge(key, CharacterClass.STACKED_KIT_ITEM_COUNT, Integer::sum);
    counts.merge(TORCH, TORCH_COUNT, Integer::sum);
    for (String key : POTIONS) counts.merge(key, POTION_COUNT, Integer::sum);
    return counts;
  }
}
