package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0092 {
  private SpawnGroup0092() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Dungeon lvl 1",
        15,
        40,
        2,
        6,
        List.of("Bat"),
        List.of(
            new SpawnGroup.SpawnPoint(212, 72, 1),
            new SpawnGroup.SpawnPoint(241, 93, 1),
            new SpawnGroup.SpawnPoint(230, 108, 1),
            new SpawnGroup.SpawnPoint(286, 113, 1),
            new SpawnGroup.SpawnPoint(284, 127, 1),
            new SpawnGroup.SpawnPoint(187, 131, 1),
            new SpawnGroup.SpawnPoint(244, 145, 1),
            new SpawnGroup.SpawnPoint(219, 191, 1),
            new SpawnGroup.SpawnPoint(166, 208, 1),
            new SpawnGroup.SpawnPoint(194, 223, 1),
            new SpawnGroup.SpawnPoint(313, 282, 1),
            new SpawnGroup.SpawnPoint(294, 304, 1),
            new SpawnGroup.SpawnPoint(371, 340, 1),
            new SpawnGroup.SpawnPoint(332, 350, 1),
            new SpawnGroup.SpawnPoint(353, 366, 1),
            new SpawnGroup.SpawnPoint(326, 408, 1)));
  }
}
