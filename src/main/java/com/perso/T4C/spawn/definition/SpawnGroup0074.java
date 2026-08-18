package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0074 {
  private SpawnGroup0074() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Gaenen Elthorn",
        8,
        8,
        1,
        0,
        List.of("GAENENELTHORN"),
        List.of(new SpawnGroup.SpawnPoint(2660, 2416, 2)));
  }
}
