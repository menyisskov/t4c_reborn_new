package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0063 {
  private SpawnGroup0063() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Oracle Guardian b",
        200,
        300,
        2,
        1,
        List.of("ORACLEGUARDIAN1B"),
        List.of(
            new SpawnGroup.SpawnPoint(2848, 2292, 2), new SpawnGroup.SpawnPoint(2824, 2316, 2)));
  }
}
