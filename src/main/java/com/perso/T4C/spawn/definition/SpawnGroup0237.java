package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0237 {
  private SpawnGroup0237() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Olin Haad Sentry",
        150,
        180,
        1,
        5,
        List.of("Olin Haad Sentry"),
        List.of(
            new SpawnGroup.SpawnPoint(2780, 1201, 0),
            new SpawnGroup.SpawnPoint(2776, 1204, 0),
            new SpawnGroup.SpawnPoint(2779, 1204, 0),
            new SpawnGroup.SpawnPoint(2784, 1205, 0),
            new SpawnGroup.SpawnPoint(2778, 1207, 0),
            new SpawnGroup.SpawnPoint(2782, 1208, 0)));
  }
}
