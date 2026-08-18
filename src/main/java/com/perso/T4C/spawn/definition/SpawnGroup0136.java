package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0136 {
  private SpawnGroup0136() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Pig",
        100,
        100,
        1,
        1,
        List.of("PIG"),
        List.of(
            new SpawnGroup.SpawnPoint(199, 762, 0),
            new SpawnGroup.SpawnPoint(196, 764, 0),
            new SpawnGroup.SpawnPoint(199, 766, 0),
            new SpawnGroup.SpawnPoint(2847, 1205, 0),
            new SpawnGroup.SpawnPoint(2850, 1205, 0),
            new SpawnGroup.SpawnPoint(2847, 1207, 0),
            new SpawnGroup.SpawnPoint(2850, 1207, 0),
            new SpawnGroup.SpawnPoint(1576, 2440, 0),
            new SpawnGroup.SpawnPoint(1578, 2443, 0),
            new SpawnGroup.SpawnPoint(1588, 2448, 0),
            new SpawnGroup.SpawnPoint(1582, 2450, 0),
            new SpawnGroup.SpawnPoint(1582, 2453, 0)));
  }
}
