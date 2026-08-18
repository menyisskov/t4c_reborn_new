package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0248 {
  private SpawnGroup0248() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Kraanian Stomper",
        30,
        60,
        3,
        5,
        List.of("Kraanian Stomper"),
        List.of(
            new SpawnGroup.SpawnPoint(2122, 2191, 1),
            new SpawnGroup.SpawnPoint(2132, 2201, 1),
            new SpawnGroup.SpawnPoint(2111, 2211, 1),
            new SpawnGroup.SpawnPoint(2099, 2221, 1),
            new SpawnGroup.SpawnPoint(2047, 2235, 1),
            new SpawnGroup.SpawnPoint(2056, 2254, 1)));
  }
}
