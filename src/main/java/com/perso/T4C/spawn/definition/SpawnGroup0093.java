package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0093 {
  private SpawnGroup0093() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Dungeon lvl 2",
        15,
        40,
        3,
        6,
        List.of("Giant Spider", "Giant Bat"),
        List.of(
            new SpawnGroup.SpawnPoint(330, 60, 1),
            new SpawnGroup.SpawnPoint(309, 66, 1),
            new SpawnGroup.SpawnPoint(281, 72, 1),
            new SpawnGroup.SpawnPoint(325, 79, 1)));
  }
}
