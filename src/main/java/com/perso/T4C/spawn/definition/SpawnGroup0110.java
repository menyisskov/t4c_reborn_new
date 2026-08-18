package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0110 {
  private SpawnGroup0110() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Ruined Castle",
        20,
        35,
        3,
        6,
        List.of("Plague Rat"),
        List.of(
            new SpawnGroup.SpawnPoint(1551, 179, 0),
            new SpawnGroup.SpawnPoint(1581, 187, 0),
            new SpawnGroup.SpawnPoint(1560, 199, 0),
            new SpawnGroup.SpawnPoint(1569, 201, 0),
            new SpawnGroup.SpawnPoint(442, 194, 1),
            new SpawnGroup.SpawnPoint(443, 210, 1),
            new SpawnGroup.SpawnPoint(441, 219, 1)));
  }
}
