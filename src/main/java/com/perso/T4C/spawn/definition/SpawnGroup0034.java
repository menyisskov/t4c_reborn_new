package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0034 {
  private SpawnGroup0034() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Obsidian Assassin",
        120,
        240,
        2,
        5,
        List.of("Obsidian Assassin"),
        List.of(
            new SpawnGroup.SpawnPoint(1115, 2574, 2),
            new SpawnGroup.SpawnPoint(1095, 2603, 2),
            new SpawnGroup.SpawnPoint(1074, 2615, 2),
            new SpawnGroup.SpawnPoint(1338, 2615, 2),
            new SpawnGroup.SpawnPoint(1083, 2617, 2),
            new SpawnGroup.SpawnPoint(1309, 2617, 2),
            new SpawnGroup.SpawnPoint(1323, 2634, 2),
            new SpawnGroup.SpawnPoint(1406, 2644, 2),
            new SpawnGroup.SpawnPoint(1373, 2670, 2),
            new SpawnGroup.SpawnPoint(1394, 2673, 2),
            new SpawnGroup.SpawnPoint(1449, 2700, 2),
            new SpawnGroup.SpawnPoint(1411, 2706, 2),
            new SpawnGroup.SpawnPoint(1460, 2734, 2)));
  }
}
