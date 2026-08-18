package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0087 {
  private SpawnGroup0087() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Venadar",
        5,
        10,
        1,
        1,
        List.of("VENADAR"),
        List.of(new SpawnGroup.SpawnPoint(2345, 545, 2)));
  }
}
