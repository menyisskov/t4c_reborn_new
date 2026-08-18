package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0265 {
  private SpawnGroup0265() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Vicar Vharmes",
        1100,
        1400,
        1,
        1,
        List.of("VICARVHARMES"),
        List.of(new SpawnGroup.SpawnPoint(1115, 2545, 1)));
  }
}
