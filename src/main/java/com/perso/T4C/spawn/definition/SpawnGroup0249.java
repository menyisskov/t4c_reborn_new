package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0249 {
  private SpawnGroup0249() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Kraanian Reaper",
        30,
        60,
        3,
        5,
        List.of("Kraanian Reaper"),
        List.of(
            new SpawnGroup.SpawnPoint(2119, 2202, 1),
            new SpawnGroup.SpawnPoint(2107, 2204, 1),
            new SpawnGroup.SpawnPoint(2117, 2212, 1)));
  }
}
