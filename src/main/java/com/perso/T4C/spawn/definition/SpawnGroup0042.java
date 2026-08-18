package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0042 {
  private SpawnGroup0042() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Quentin Tarantula",
        1300,
        1800,
        1,
        5,
        List.of("MOBQUENTINTARANTULA"),
        List.of(new SpawnGroup.SpawnPoint(1731, 247, 2)));
  }
}
