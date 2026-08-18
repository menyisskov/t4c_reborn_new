package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0084 {
  private SpawnGroup0084() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Mad Patient And Loon",
        40,
        80,
        2,
        5,
        List.of("MOBMADPATIENT", "MOBLOON"),
        List.of(
            new SpawnGroup.SpawnPoint(2724, 2206, 0),
            new SpawnGroup.SpawnPoint(2711, 2217, 0),
            new SpawnGroup.SpawnPoint(2726, 2223, 0),
            new SpawnGroup.SpawnPoint(2751, 2231, 0),
            new SpawnGroup.SpawnPoint(2735, 2248, 0),
            new SpawnGroup.SpawnPoint(2725, 2290, 0),
            new SpawnGroup.SpawnPoint(2726, 2308, 0),
            new SpawnGroup.SpawnPoint(2744, 2310, 0),
            new SpawnGroup.SpawnPoint(2713, 2315, 0),
            new SpawnGroup.SpawnPoint(2748, 2322, 0),
            new SpawnGroup.SpawnPoint(2728, 2330, 0),
            new SpawnGroup.SpawnPoint(2734, 2339, 0),
            new SpawnGroup.SpawnPoint(2723, 2378, 0),
            new SpawnGroup.SpawnPoint(2723, 2392, 0),
            new SpawnGroup.SpawnPoint(2712, 2401, 0),
            new SpawnGroup.SpawnPoint(2725, 2415, 0),
            new SpawnGroup.SpawnPoint(2721, 2469, 0),
            new SpawnGroup.SpawnPoint(2728, 2504, 0),
            new SpawnGroup.SpawnPoint(2731, 2507, 0),
            new SpawnGroup.SpawnPoint(2728, 2590, 0),
            new SpawnGroup.SpawnPoint(2735, 2596, 0)));
  }
}
