package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0059 {
  private SpawnGroup0059() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Bthonian",
        30,
        60,
        1,
        0,
        List.of("Bthonian"),
        List.of(
            new SpawnGroup.SpawnPoint(2808, 2436, 2),
            new SpawnGroup.SpawnPoint(2804, 2440, 2),
            new SpawnGroup.SpawnPoint(2800, 2444, 2),
            new SpawnGroup.SpawnPoint(2816, 2444, 2),
            new SpawnGroup.SpawnPoint(2812, 2448, 2),
            new SpawnGroup.SpawnPoint(2792, 2452, 2),
            new SpawnGroup.SpawnPoint(2808, 2452, 2),
            new SpawnGroup.SpawnPoint(2796, 2456, 2),
            new SpawnGroup.SpawnPoint(2800, 2460, 2)));
  }
}
