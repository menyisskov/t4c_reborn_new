package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0178 {
  private SpawnGroup0178() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "T-Bone",
        1100,
        1300,
        1,
        6,
        List.of("T-Bone"),
        List.of(new SpawnGroup.SpawnPoint(189, 2975, 2)));
  }
}
