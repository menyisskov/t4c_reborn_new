package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0239 {
  private SpawnGroup0239() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Anthor The Mad",
        600,
        600,
        1,
        1,
        List.of("ANTHORTHEMAD"),
        List.of(new SpawnGroup.SpawnPoint(881, 653, 0)));
  }
}
