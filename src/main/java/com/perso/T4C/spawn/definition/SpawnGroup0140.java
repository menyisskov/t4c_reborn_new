package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0140 {
  private SpawnGroup0140() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Bonedead",
        1100,
        1300,
        1,
        5,
        List.of("Bonedead"),
        List.of(new SpawnGroup.SpawnPoint(442, 164, 1)));
  }
}
