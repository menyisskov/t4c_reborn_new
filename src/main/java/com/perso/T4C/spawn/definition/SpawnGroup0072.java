package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0072 {
  private SpawnGroup0072() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Oracle Gate Guardian 1",
        30,
        60,
        1,
        0,
        List.of("Oracle Gate Guardian 1"),
        List.of(
            new SpawnGroup.SpawnPoint(2848, 2224, 2),
            new SpawnGroup.SpawnPoint(2836, 2236, 2),
            new SpawnGroup.SpawnPoint(2816, 2240, 2),
            new SpawnGroup.SpawnPoint(2848, 2240, 2),
            new SpawnGroup.SpawnPoint(2836, 2244, 2),
            new SpawnGroup.SpawnPoint(2828, 2252, 2),
            new SpawnGroup.SpawnPoint(2808, 2256, 2),
            new SpawnGroup.SpawnPoint(2820, 2260, 2)));
  }
}
