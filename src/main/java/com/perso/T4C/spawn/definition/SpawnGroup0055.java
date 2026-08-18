package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0055 {
  private SpawnGroup0055() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Moon Tug Sentry 2",
        500,
        1000,
        1,
        1,
        List.of("MOONTUGGUARD2"),
        List.of(
            new SpawnGroup.SpawnPoint(2580, 261, 2),
            new SpawnGroup.SpawnPoint(2590, 283, 2),
            new SpawnGroup.SpawnPoint(2554, 286, 2),
            new SpawnGroup.SpawnPoint(2612, 290, 2),
            new SpawnGroup.SpawnPoint(2578, 293, 2),
            new SpawnGroup.SpawnPoint(2589, 314, 2)));
  }
}
