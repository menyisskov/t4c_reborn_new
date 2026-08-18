package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0054 {
  private SpawnGroup0054() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Moon Tug Sentry 1",
        500,
        1000,
        1,
        1,
        List.of("MOONTUGGUARD1"),
        List.of(
            new SpawnGroup.SpawnPoint(1947, 1135, 2), new SpawnGroup.SpawnPoint(1936, 1145, 2)));
  }
}
