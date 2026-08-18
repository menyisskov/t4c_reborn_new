package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0116 {
  private SpawnGroup0116() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Forest Guardian",
        1000,
        2000,
        1,
        7,
        List.of("Forest Guardian"),
        List.of(
            new SpawnGroup.SpawnPoint(2800, 115, 0),
            new SpawnGroup.SpawnPoint(2727, 181, 0),
            new SpawnGroup.SpawnPoint(2722, 189, 0),
            new SpawnGroup.SpawnPoint(2736, 209, 0),
            new SpawnGroup.SpawnPoint(2745, 220, 0),
            new SpawnGroup.SpawnPoint(2839, 223, 0),
            new SpawnGroup.SpawnPoint(1399, 2663, 0),
            new SpawnGroup.SpawnPoint(1400, 2693, 0),
            new SpawnGroup.SpawnPoint(1432, 2693, 0),
            new SpawnGroup.SpawnPoint(1446, 2695, 0),
            new SpawnGroup.SpawnPoint(1357, 2718, 0),
            new SpawnGroup.SpawnPoint(1470, 2734, 0),
            new SpawnGroup.SpawnPoint(1484, 2753, 0),
            new SpawnGroup.SpawnPoint(1263, 2811, 0),
            new SpawnGroup.SpawnPoint(1462, 2818, 0),
            new SpawnGroup.SpawnPoint(1335, 2856, 0),
            new SpawnGroup.SpawnPoint(1357, 2859, 0),
            new SpawnGroup.SpawnPoint(1378, 2860, 0),
            new SpawnGroup.SpawnPoint(1313, 2882, 0),
            new SpawnGroup.SpawnPoint(1502, 2883, 0),
            new SpawnGroup.SpawnPoint(1356, 2886, 0),
            new SpawnGroup.SpawnPoint(1428, 2900, 0),
            new SpawnGroup.SpawnPoint(1289, 2903, 0)));
  }
}
