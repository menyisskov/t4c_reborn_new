package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0253 {
  private SpawnGroup0253() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Fallen Warrior",
        30,
        60,
        3,
        5,
        List.of("Fallen Warrior"),
        List.of(
            new SpawnGroup.SpawnPoint(810, 1495, 1),
            new SpawnGroup.SpawnPoint(810, 1500, 1),
            new SpawnGroup.SpawnPoint(851, 1517, 1),
            new SpawnGroup.SpawnPoint(862, 1523, 1),
            new SpawnGroup.SpawnPoint(871, 1525, 1),
            new SpawnGroup.SpawnPoint(857, 1529, 1),
            new SpawnGroup.SpawnPoint(824, 1541, 1),
            new SpawnGroup.SpawnPoint(841, 1544, 1),
            new SpawnGroup.SpawnPoint(860, 1545, 1),
            new SpawnGroup.SpawnPoint(853, 1546, 1),
            new SpawnGroup.SpawnPoint(834, 1550, 1),
            new SpawnGroup.SpawnPoint(837, 1558, 1)));
  }
}
