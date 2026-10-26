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
}
