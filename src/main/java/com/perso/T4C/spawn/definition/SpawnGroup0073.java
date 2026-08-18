package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0073 {
  private SpawnGroup0073() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Oracle Gate Guardian 2",
        30,
        60,
        1,
        0,
        List.of("Oracle Gate Guardian 2"),
        List.of(
            new SpawnGroup.SpawnPoint(2788, 2276, 2),
            new SpawnGroup.SpawnPoint(2772, 2284, 2),
            new SpawnGroup.SpawnPoint(2792, 2288, 2),
            new SpawnGroup.SpawnPoint(2784, 2296, 2),
            new SpawnGroup.SpawnPoint(2768, 2304, 2),
            new SpawnGroup.SpawnPoint(2776, 2304, 2),
            new SpawnGroup.SpawnPoint(2756, 2316, 2),
            new SpawnGroup.SpawnPoint(2772, 2316, 2)));
  }
}
