package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0161 {
  private SpawnGroup0161() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Edgar",
        1800,
        1900,
        1,
        5,
        List.of("EDGAR"),
        List.of(new SpawnGroup.SpawnPoint(2855, 1162, 0)));
  }
}
