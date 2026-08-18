package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0225 {
  private SpawnGroup0225() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Flesh golem",
        1800,
        2700,
        1,
        5,
        List.of("MOBFLESHGOLEM"),
        List.of(new SpawnGroup.SpawnPoint(74, 1569, 0)));
  }
}
