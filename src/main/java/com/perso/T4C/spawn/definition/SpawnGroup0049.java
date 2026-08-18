package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0049 {
  private SpawnGroup0049() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Berserker Rat",
        50,
        75,
        1,
        5,
        List.of("Berserker Rat"),
        List.of(
            new SpawnGroup.SpawnPoint(1524, 385, 2),
            new SpawnGroup.SpawnPoint(1556, 387, 2),
            new SpawnGroup.SpawnPoint(1512, 395, 2),
            new SpawnGroup.SpawnPoint(1508, 412, 2),
            new SpawnGroup.SpawnPoint(1519, 415, 2),
            new SpawnGroup.SpawnPoint(1535, 428, 2),
            new SpawnGroup.SpawnPoint(1510, 436, 2),
            new SpawnGroup.SpawnPoint(1551, 445, 2),
            new SpawnGroup.SpawnPoint(1487, 461, 2),
            new SpawnGroup.SpawnPoint(1507, 484, 2),
            new SpawnGroup.SpawnPoint(1492, 488, 2),
            new SpawnGroup.SpawnPoint(1457, 489, 2),
            new SpawnGroup.SpawnPoint(1474, 489, 2),
            new SpawnGroup.SpawnPoint(1469, 498, 2),
            new SpawnGroup.SpawnPoint(1456, 501, 2),
            new SpawnGroup.SpawnPoint(1490, 501, 2),
            new SpawnGroup.SpawnPoint(1478, 510, 2)));
  }
}
