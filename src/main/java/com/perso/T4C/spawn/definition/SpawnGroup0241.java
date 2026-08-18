package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0241 {
  private SpawnGroup0241() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Mercenary C",
        15,
        30,
        2,
        5,
        List.of("MOBMERCENARYC"),
        List.of(
            new SpawnGroup.SpawnPoint(2815, 964, 0),
            new SpawnGroup.SpawnPoint(2821, 965, 0),
            new SpawnGroup.SpawnPoint(2831, 965, 0),
            new SpawnGroup.SpawnPoint(2808, 970, 0),
            new SpawnGroup.SpawnPoint(2795, 972, 0),
            new SpawnGroup.SpawnPoint(2800, 978, 0),
            new SpawnGroup.SpawnPoint(2809, 978, 0),
            new SpawnGroup.SpawnPoint(2796, 986, 0)));
  }
}
