package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0062 {
  private SpawnGroup0062() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Oracle Guardian a",
        200,
        300,
        2,
        1,
        List.of("ORACLEGUARDIAN1A"),
        List.of(
            new SpawnGroup.SpawnPoint(2872, 2292, 2), new SpawnGroup.SpawnPoint(2824, 2340, 2)));
  }
}
