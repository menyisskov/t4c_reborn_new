package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0118 {
  private SpawnGroup0118() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Tomb Raider",
        900,
        1150,
        1,
        6,
        List.of("Tomb Raider"),
        List.of(
            new SpawnGroup.SpawnPoint(1121, 129, 1),
            new SpawnGroup.SpawnPoint(428, 204, 1),
            new SpawnGroup.SpawnPoint(21, 461, 1),
            new SpawnGroup.SpawnPoint(71, 620, 1)));
  }
}
