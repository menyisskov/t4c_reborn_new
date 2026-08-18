package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0117 {
  private SpawnGroup0117() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Griroesh",
        900,
        1100,
        1,
        6,
        List.of("Griroesh"),
        List.of(new SpawnGroup.SpawnPoint(1096, 157, 1), new SpawnGroup.SpawnPoint(1131, 159, 1)));
  }
}
