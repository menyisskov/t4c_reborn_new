package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0183 {
  private SpawnGroup0183() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Death Jester",
        1100,
        1300,
        1,
        5,
        List.of("Death Jester"),
        List.of(new SpawnGroup.SpawnPoint(183, 2328, 1)));
  }
}
