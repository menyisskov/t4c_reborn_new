package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0179 {
  private SpawnGroup0179() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Ratsputin",
        1100,
        1300,
        1,
        6,
        List.of("Ratsputin"),
        List.of(new SpawnGroup.SpawnPoint(963, 581, 2)));
  }
}
