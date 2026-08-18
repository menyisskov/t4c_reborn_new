package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0200 {
  private SpawnGroup0200() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Graax",
        800,
        1300,
        1,
        5,
        List.of("Graax"),
        List.of(new SpawnGroup.SpawnPoint(315, 464, 2)));
  }
}
