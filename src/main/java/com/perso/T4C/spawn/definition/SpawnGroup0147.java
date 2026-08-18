package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0147 {
  private SpawnGroup0147() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Orc Deserter",
        25,
        40,
        1,
        5,
        List.of("Orc Deserter"),
        List.of(
            new SpawnGroup.SpawnPoint(1069, 1917, 0),
            new SpawnGroup.SpawnPoint(1037, 1934, 0),
            new SpawnGroup.SpawnPoint(1118, 1951, 0),
            new SpawnGroup.SpawnPoint(1109, 1981, 0),
            new SpawnGroup.SpawnPoint(1104, 2010, 0),
            new SpawnGroup.SpawnPoint(1051, 2038, 0),
            new SpawnGroup.SpawnPoint(1157, 2044, 0),
            new SpawnGroup.SpawnPoint(1158, 2044, 0),
            new SpawnGroup.SpawnPoint(1159, 2053, 0),
            new SpawnGroup.SpawnPoint(1002, 2088, 0),
            new SpawnGroup.SpawnPoint(1010, 2131, 0)));
  }
}
