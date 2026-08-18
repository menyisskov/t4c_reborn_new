package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0067 {
  private SpawnGroup0067() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Oracle Guardian f",
        200,
        300,
        2,
        1,
        List.of("ORACLEGUARDIAN1F"),
        List.of(
            new SpawnGroup.SpawnPoint(2860, 2280, 2), new SpawnGroup.SpawnPoint(2812, 2328, 2)));
  }
}
