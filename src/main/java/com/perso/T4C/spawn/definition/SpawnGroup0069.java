package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0069 {
  private SpawnGroup0069() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Quickness Guardian",
        60,
        120,
        1,
        0,
        List.of("Oracle Quickness Guardian"),
        List.of(
            new SpawnGroup.SpawnPoint(2816, 2200, 2),
            new SpawnGroup.SpawnPoint(2828, 2204, 2),
            new SpawnGroup.SpawnPoint(2812, 2212, 2),
            new SpawnGroup.SpawnPoint(2800, 2216, 2),
            new SpawnGroup.SpawnPoint(2824, 2216, 2),
            new SpawnGroup.SpawnPoint(2784, 2232, 2),
            new SpawnGroup.SpawnPoint(2804, 2236, 2),
            new SpawnGroup.SpawnPoint(2792, 2247, 2),
            new SpawnGroup.SpawnPoint(2764, 2252, 2),
            new SpawnGroup.SpawnPoint(2780, 2260, 2),
            new SpawnGroup.SpawnPoint(2748, 2268, 2),
            new SpawnGroup.SpawnPoint(2768, 2272, 2),
            new SpawnGroup.SpawnPoint(2744, 2280, 2),
            new SpawnGroup.SpawnPoint(2732, 2284, 2),
            new SpawnGroup.SpawnPoint(2748, 2292, 2),
            new SpawnGroup.SpawnPoint(2736, 2296, 2)));
  }
}
