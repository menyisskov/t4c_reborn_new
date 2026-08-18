package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0187 {
  private SpawnGroup0187() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Bulldaoza",
        1000,
        1150,
        1,
        5,
        List.of("Bulldaoza"),
        List.of(new SpawnGroup.SpawnPoint(375, 987, 1)));
  }
}
