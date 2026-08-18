package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0236 {
  private SpawnGroup0236() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Olin Haad Brigand",
        150,
        180,
        1,
        5,
        List.of("Olin Haad Brigand"),
        List.of(
            new SpawnGroup.SpawnPoint(2605, 1198, 0),
            new SpawnGroup.SpawnPoint(2603, 1200, 0),
            new SpawnGroup.SpawnPoint(2607, 1200, 0),
            new SpawnGroup.SpawnPoint(2604, 1202, 0),
            new SpawnGroup.SpawnPoint(2606, 1202, 0),
            new SpawnGroup.SpawnPoint(2613, 1209, 0),
            new SpawnGroup.SpawnPoint(2621, 1215, 0),
            new SpawnGroup.SpawnPoint(2633, 1219, 0)));
  }
}
