package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0065 {
  private SpawnGroup0065() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Oracle Guardian d",
        200,
        300,
        2,
        1,
        List.of("ORACLEGUARDIAN1D"),
        List.of(
            new SpawnGroup.SpawnPoint(2872, 2268, 2), new SpawnGroup.SpawnPoint(2800, 2340, 2)));
  }
}
