package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0071 {
  private SpawnGroup0071() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Invulnerable Guardian",
        1,
        1,
        1,
        0,
        List.of("Oracle Invulnerable Guardian"),
        List.of(
            new SpawnGroup.SpawnPoint(2796, 2184, 2),
            new SpawnGroup.SpawnPoint(2788, 2192, 2),
            new SpawnGroup.SpawnPoint(2808, 2196, 2),
            new SpawnGroup.SpawnPoint(2794, 2198, 2),
            new SpawnGroup.SpawnPoint(2780, 2200, 2),
            new SpawnGroup.SpawnPoint(2800, 2204, 2),
            new SpawnGroup.SpawnPoint(2786, 2206, 2),
            new SpawnGroup.SpawnPoint(2792, 2212, 2),
            new SpawnGroup.SpawnPoint(2732, 2248, 2),
            new SpawnGroup.SpawnPoint(2738, 2254, 2),
            new SpawnGroup.SpawnPoint(2724, 2256, 2),
            new SpawnGroup.SpawnPoint(2744, 2260, 2),
            new SpawnGroup.SpawnPoint(2730, 2262, 2),
            new SpawnGroup.SpawnPoint(2716, 2264, 2),
            new SpawnGroup.SpawnPoint(2736, 2268, 2),
            new SpawnGroup.SpawnPoint(2728, 2276, 2)));
  }
}
