package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0181 {
  private SpawnGroup0181() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Dark Axe",
        1100,
        1300,
        1,
        5,
        List.of("Dark Axe"),
        List.of(new SpawnGroup.SpawnPoint(1359, 273, 1)));
  }
}
