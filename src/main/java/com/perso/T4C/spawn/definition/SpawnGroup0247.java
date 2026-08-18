package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0247 {
  private SpawnGroup0247() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Kraanian Plague",
        30,
        60,
        3,
        5,
        List.of("Kraanian Plague"),
        List.of(
            new SpawnGroup.SpawnPoint(1111, 1730, 1),
            new SpawnGroup.SpawnPoint(2124, 2198, 1),
            new SpawnGroup.SpawnPoint(2115, 2205, 1),
            new SpawnGroup.SpawnPoint(2084, 2217, 1),
            new SpawnGroup.SpawnPoint(2067, 2223, 1),
            new SpawnGroup.SpawnPoint(2052, 2225, 1),
            new SpawnGroup.SpawnPoint(2116, 2226, 1),
            new SpawnGroup.SpawnPoint(2095, 2227, 1),
            new SpawnGroup.SpawnPoint(2101, 2235, 1),
            new SpawnGroup.SpawnPoint(2080, 2240, 1),
            new SpawnGroup.SpawnPoint(2099, 2256, 1),
            new SpawnGroup.SpawnPoint(2073, 2268, 1),
            new SpawnGroup.SpawnPoint(2086, 2273, 1)));
  }
}
