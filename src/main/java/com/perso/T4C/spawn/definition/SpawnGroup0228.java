package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0228 {
  private SpawnGroup0228() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Red Eyed Centaur",
        120,
        360,
        2,
        5,
        List.of("MOBREDEYEDCENTAUR"),
        List.of(
            new SpawnGroup.SpawnPoint(973, 962, 0),
            new SpawnGroup.SpawnPoint(971, 968, 0),
            new SpawnGroup.SpawnPoint(1089, 1014, 0),
            new SpawnGroup.SpawnPoint(926, 1082, 0)));
  }
}
