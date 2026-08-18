package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0234 {
  private SpawnGroup0234() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Olin Haad Elite Guard",
        15,
        30,
        1,
        1,
        List.of("OLINHAADELITEGUARD"),
        List.of(
            new SpawnGroup.SpawnPoint(1815, 2532, 1),
            new SpawnGroup.SpawnPoint(1807, 2540, 1),
            new SpawnGroup.SpawnPoint(1843, 2560, 1),
            new SpawnGroup.SpawnPoint(1835, 2568, 1)));
  }
}
