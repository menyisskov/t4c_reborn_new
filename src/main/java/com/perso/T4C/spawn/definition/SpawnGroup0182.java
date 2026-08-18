package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0182 {
  private SpawnGroup0182() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Delwobble",
        1100,
        1300,
        1,
        5,
        List.of("MOBDELWOBBLE"),
        List.of(new SpawnGroup.SpawnPoint(1523, 63, 1)));
  }
}
