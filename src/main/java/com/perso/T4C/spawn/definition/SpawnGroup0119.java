package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0119 {
  private SpawnGroup0119() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Balork",
        850,
        900,
        1,
        6,
        List.of("BALORK"),
        List.of(new SpawnGroup.SpawnPoint(235, 452, 1)));
  }
}
