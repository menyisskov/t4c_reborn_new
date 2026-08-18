package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0264 {
  private SpawnGroup0264() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Vicar Ramiel",
        1100,
        1400,
        1,
        1,
        List.of("VICARRAMIEL"),
        List.of(new SpawnGroup.SpawnPoint(1421, 2223, 1)));
  }
}
