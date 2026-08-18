package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0090 {
  private SpawnGroup0090() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Makrsh Ptangh Spawner",
        120,
        360,
        1,
        0,
        List.of("MAKRSHPTANGHSPAWNER"),
        List.of(
            new SpawnGroup.SpawnPoint(2215, 245, 1),
            new SpawnGroup.SpawnPoint(2315, 245, 1),
            new SpawnGroup.SpawnPoint(2215, 345, 1),
            new SpawnGroup.SpawnPoint(2315, 345, 1)));
  }
}
