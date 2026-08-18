package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0217 {
  private SpawnGroup0217() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Grott",
        800,
        1300,
        1,
        5,
        List.of("Grott"),
        List.of(new SpawnGroup.SpawnPoint(1058, 2431, 0)));
  }
}
