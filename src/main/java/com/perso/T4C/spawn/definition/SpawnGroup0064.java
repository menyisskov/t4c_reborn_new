package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0064 {
  private SpawnGroup0064() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Oracle Guardian c",
        200,
        300,
        2,
        1,
        List.of("ORACLEGUARDIAN1C"),
        List.of(
            new SpawnGroup.SpawnPoint(2884, 2280, 2), new SpawnGroup.SpawnPoint(2812, 2352, 2)));
  }
}
