package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0026 {
  private SpawnGroup0026() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Warg",
        60,
        120,
        1,
        5,
        List.of("Warg"),
        List.of(
            new SpawnGroup.SpawnPoint(806, 533, 0),
            new SpawnGroup.SpawnPoint(819, 541, 0),
            new SpawnGroup.SpawnPoint(844, 857, 0),
            new SpawnGroup.SpawnPoint(814, 868, 0),
            new SpawnGroup.SpawnPoint(562, 1081, 0),
            new SpawnGroup.SpawnPoint(572, 1087, 0),
            new SpawnGroup.SpawnPoint(818, 1097, 0),
            new SpawnGroup.SpawnPoint(685, 1114, 0)));
  }
}
