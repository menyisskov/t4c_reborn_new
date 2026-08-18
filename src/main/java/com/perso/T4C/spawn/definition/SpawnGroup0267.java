package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0267 {
  private SpawnGroup0267() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Harvester Of Life",
        120,
        120,
        1,
        0,
        List.of("HARVESTEROFLIFE"),
        List.of(new SpawnGroup.SpawnPoint(2710, 1040, 1)));
  }
}
