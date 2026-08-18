package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0177 {
  private SpawnGroup0177() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Count Hemogoblin",
        1100,
        1300,
        1,
        6,
        List.of("Count Hemogoblin"),
        List.of(new SpawnGroup.SpawnPoint(371, 2523, 2)));
  }
}
