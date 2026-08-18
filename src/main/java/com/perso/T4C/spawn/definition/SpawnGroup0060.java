package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0060 {
  private SpawnGroup0060() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Bthurkhan",
        30,
        60,
        1,
        0,
        List.of("Bthurkhan"),
        List.of(
            new SpawnGroup.SpawnPoint(2752, 2484, 2),
            new SpawnGroup.SpawnPoint(2744, 2492, 2),
            new SpawnGroup.SpawnPoint(2760, 2492, 2),
            new SpawnGroup.SpawnPoint(2752, 2500, 2)));
  }
}
