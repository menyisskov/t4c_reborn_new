package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0227 {
  private SpawnGroup0227() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Runaway Patient",
        1000,
        2000,
        1,
        5,
        List.of("MOBRUNAWAYPATIENT"),
        List.of(new SpawnGroup.SpawnPoint(2445, 260, 0)));
  }
}
