package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0086 {
  private SpawnGroup0086() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Madman and Lunatic and Orderly",
        40,
        80,
        2,
        5,
        List.of("MOBMADMAN", "MOBRAVINGLUNATIC", "MOBDERANGEDORDERLY"),
        List.of(
            new SpawnGroup.SpawnPoint(2792, 2222, 0),
            new SpawnGroup.SpawnPoint(2796, 2227, 0),
            new SpawnGroup.SpawnPoint(2809, 2292, 0),
            new SpawnGroup.SpawnPoint(2797, 2315, 0),
            new SpawnGroup.SpawnPoint(2817, 2315, 0),
            new SpawnGroup.SpawnPoint(2816, 2337, 0),
            new SpawnGroup.SpawnPoint(2816, 2385, 0),
            new SpawnGroup.SpawnPoint(2796, 2402, 0),
            new SpawnGroup.SpawnPoint(2815, 2418, 0),
            new SpawnGroup.SpawnPoint(2816, 2472, 0),
            new SpawnGroup.SpawnPoint(2826, 2480, 0),
            new SpawnGroup.SpawnPoint(2792, 2483, 0),
            new SpawnGroup.SpawnPoint(2813, 2485, 0),
            new SpawnGroup.SpawnPoint(2798, 2490, 0),
            new SpawnGroup.SpawnPoint(2831, 2492, 0),
            new SpawnGroup.SpawnPoint(2812, 2506, 0),
            new SpawnGroup.SpawnPoint(2809, 2552, 0),
            new SpawnGroup.SpawnPoint(2705, 2567, 0),
            new SpawnGroup.SpawnPoint(2828, 2568, 0),
            new SpawnGroup.SpawnPoint(2794, 2573, 0),
            new SpawnGroup.SpawnPoint(2844, 2580, 0),
            new SpawnGroup.SpawnPoint(2832, 2582, 0),
            new SpawnGroup.SpawnPoint(2819, 2593, 0)));
  }
}
