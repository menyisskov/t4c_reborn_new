package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0081 {
  private SpawnGroup0081() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Dr Von Pyre",
        800,
        1200,
        1,
        5,
        List.of("DRVONPYRE"),
        List.of(new SpawnGroup.SpawnPoint(2831, 2397, 0)));
  }
}
