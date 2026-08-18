package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0240 {
  private SpawnGroup0240() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Mercenary B",
        15,
        30,
        2,
        5,
        List.of("MOBMERCENARYB"),
        List.of(
            new SpawnGroup.SpawnPoint(2736, 957, 0),
            new SpawnGroup.SpawnPoint(2738, 963, 0),
            new SpawnGroup.SpawnPoint(2740, 970, 0),
            new SpawnGroup.SpawnPoint(2748, 971, 0),
            new SpawnGroup.SpawnPoint(2733, 972, 0),
            new SpawnGroup.SpawnPoint(2742, 974, 0),
            new SpawnGroup.SpawnPoint(2728, 977, 0),
            new SpawnGroup.SpawnPoint(2748, 979, 0)));
  }
}
