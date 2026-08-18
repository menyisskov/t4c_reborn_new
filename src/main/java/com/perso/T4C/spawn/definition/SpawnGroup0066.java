package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0066 {
  private SpawnGroup0066() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Oracle Guardian e",
        200,
        300,
        2,
        1,
        List.of("ORACLEGUARDIAN1E"),
        List.of(
            new SpawnGroup.SpawnPoint(2860, 2304, 2), new SpawnGroup.SpawnPoint(2836, 2328, 2)));
  }
}
