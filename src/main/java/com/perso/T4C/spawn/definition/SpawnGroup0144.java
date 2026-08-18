package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0144 {
  private SpawnGroup0144() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Dune Raider",
        30,
        45,
        1,
        5,
        List.of("Dune Raider"),
        List.of(
            new SpawnGroup.SpawnPoint(233, 2406, 0),
            new SpawnGroup.SpawnPoint(199, 2410, 0),
            new SpawnGroup.SpawnPoint(199, 2411, 0),
            new SpawnGroup.SpawnPoint(511, 2413, 0),
            new SpawnGroup.SpawnPoint(529, 2426, 0),
            new SpawnGroup.SpawnPoint(480, 2427, 0),
            new SpawnGroup.SpawnPoint(199, 2428, 0),
            new SpawnGroup.SpawnPoint(177, 2448, 0),
            new SpawnGroup.SpawnPoint(434, 2461, 0),
            new SpawnGroup.SpawnPoint(275, 2462, 0),
            new SpawnGroup.SpawnPoint(274, 2516, 0),
            new SpawnGroup.SpawnPoint(282, 2517, 0),
            new SpawnGroup.SpawnPoint(146, 2532, 0),
            new SpawnGroup.SpawnPoint(363, 2537, 0),
            new SpawnGroup.SpawnPoint(402, 2542, 0),
            new SpawnGroup.SpawnPoint(165, 2570, 0),
            new SpawnGroup.SpawnPoint(249, 2605, 0),
            new SpawnGroup.SpawnPoint(354, 2667, 0)));
  }
}
