package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0038 {
  private SpawnGroup0038() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Purifier",
        120,
        240,
        1,
        7,
        List.of("MOBPURIFIER"),
        List.of(
            new SpawnGroup.SpawnPoint(2031, 2732, 0),
            new SpawnGroup.SpawnPoint(1920, 2745, 0),
            new SpawnGroup.SpawnPoint(1948, 2787, 0),
            new SpawnGroup.SpawnPoint(1989, 2811, 0),
            new SpawnGroup.SpawnPoint(1929, 2817, 0),
            new SpawnGroup.SpawnPoint(1954, 2844, 0),
            new SpawnGroup.SpawnPoint(1881, 2862, 0),
            new SpawnGroup.SpawnPoint(1846, 2869, 0),
            new SpawnGroup.SpawnPoint(1919, 2939, 0)));
  }
}
