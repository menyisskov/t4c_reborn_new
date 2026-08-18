package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0095 {
  private SpawnGroup0095() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Dungeon lvl 4",
        15,
        40,
        2,
        7,
        List.of("Green Slime", "Atrocity"),
        List.of(
            new SpawnGroup.SpawnPoint(126, 261, 1),
            new SpawnGroup.SpawnPoint(198, 296, 1),
            new SpawnGroup.SpawnPoint(182, 315, 1),
            new SpawnGroup.SpawnPoint(189, 328, 1),
            new SpawnGroup.SpawnPoint(191, 360, 1),
            new SpawnGroup.SpawnPoint(228, 420, 1),
            new SpawnGroup.SpawnPoint(210, 446, 1),
            new SpawnGroup.SpawnPoint(181, 448, 1),
            new SpawnGroup.SpawnPoint(149, 451, 1),
            new SpawnGroup.SpawnPoint(277, 466, 1),
            new SpawnGroup.SpawnPoint(196, 471, 1),
            new SpawnGroup.SpawnPoint(269, 475, 1),
            new SpawnGroup.SpawnPoint(235, 483, 1),
            new SpawnGroup.SpawnPoint(255, 483, 1),
            new SpawnGroup.SpawnPoint(221, 492, 1),
            new SpawnGroup.SpawnPoint(210, 509, 1),
            new SpawnGroup.SpawnPoint(257, 536, 1),
            new SpawnGroup.SpawnPoint(172, 537, 1),
            new SpawnGroup.SpawnPoint(241, 537, 1)));
  }
}
