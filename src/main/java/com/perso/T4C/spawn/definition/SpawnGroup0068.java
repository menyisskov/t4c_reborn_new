package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0068 {
  private SpawnGroup0068() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Time Guardian",
        30,
        60,
        1,
        0,
        List.of("Time Guardian"),
        List.of(
            new SpawnGroup.SpawnPoint(2856, 2244, 2),
            new SpawnGroup.SpawnPoint(2868, 2256, 2),
            new SpawnGroup.SpawnPoint(2842, 2258, 2),
            new SpawnGroup.SpawnPoint(2848, 2264, 2),
            new SpawnGroup.SpawnPoint(2854, 2270, 2),
            new SpawnGroup.SpawnPoint(2828, 2272, 2),
            new SpawnGroup.SpawnPoint(2840, 2284, 2),
            new SpawnGroup.SpawnPoint(2804, 2296, 2),
            new SpawnGroup.SpawnPoint(2816, 2308, 2),
            new SpawnGroup.SpawnPoint(2790, 2310, 2),
            new SpawnGroup.SpawnPoint(2796, 2316, 2),
            new SpawnGroup.SpawnPoint(2802, 2322, 2),
            new SpawnGroup.SpawnPoint(2776, 2324, 2),
            new SpawnGroup.SpawnPoint(2788, 2336, 2)));
  }
}
