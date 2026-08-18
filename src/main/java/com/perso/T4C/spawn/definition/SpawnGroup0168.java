package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0168 {
  private SpawnGroup0168() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Dwarthon Stoneface",
        2000,
        2000,
        1,
        5,
        List.of("DWARTHONSTONEFACE"),
        List.of(new SpawnGroup.SpawnPoint(303, 1678, 0)));
  }
}
