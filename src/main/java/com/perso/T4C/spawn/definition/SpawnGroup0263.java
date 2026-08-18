package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0263 {
  private SpawnGroup0263() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Vicar Kervian",
        1100,
        1400,
        1,
        1,
        List.of("VICARKERVIAN"),
        List.of(new SpawnGroup.SpawnPoint(1470, 2594, 1)));
  }
}
