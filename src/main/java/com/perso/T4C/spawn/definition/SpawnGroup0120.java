package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0120 {
  private SpawnGroup0120() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Renegade Orc",
        1000,
        1300,
        1,
        6,
        List.of("RENEGADEORCLEADER"),
        List.of(new SpawnGroup.SpawnPoint(1076, 276, 1)));
  }
}
