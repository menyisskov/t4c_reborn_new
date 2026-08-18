package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0162 {
  private SpawnGroup0162() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Sandlord",
        1200,
        1200,
        1,
        5,
        List.of("Sandlord"),
        List.of(new SpawnGroup.SpawnPoint(266, 2400, 0)));
  }
}
