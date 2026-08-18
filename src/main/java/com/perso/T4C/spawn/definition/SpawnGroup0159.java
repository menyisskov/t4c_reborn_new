package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0159 {
  private SpawnGroup0159() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Burly Jail Keeper",
        20,
        35,
        1,
        5,
        List.of("Jailkeeper"),
        List.of(
            new SpawnGroup.SpawnPoint(778, 517, 1),
            new SpawnGroup.SpawnPoint(255, 2358, 1),
            new SpawnGroup.SpawnPoint(204, 2371, 1),
            new SpawnGroup.SpawnPoint(236, 2408, 1)));
  }
}
