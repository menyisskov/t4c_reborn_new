package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0206 {
  private SpawnGroup0206() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Fugar the smelly",
        800,
        1300,
        1,
        5,
        List.of("Fugar"),
        List.of(new SpawnGroup.SpawnPoint(640, 2225, 2)));
  }
}
