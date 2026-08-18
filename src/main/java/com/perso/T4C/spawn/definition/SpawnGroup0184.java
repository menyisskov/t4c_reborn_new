package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0184 {
  private SpawnGroup0184() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Skullfire",
        1100,
        1300,
        1,
        5,
        List.of("Skullfire"),
        List.of(new SpawnGroup.SpawnPoint(553, 2117, 1)));
  }
}
