package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0016 {
  private SpawnGroup0016() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Centaur Champion",
        100,
        300,
        1,
        5,
        List.of("CENTAURCHAMPION"),
        List.of(
            new SpawnGroup.SpawnPoint(851, 986, 0),
            new SpawnGroup.SpawnPoint(1020, 989, 0),
            new SpawnGroup.SpawnPoint(1064, 1090, 0),
            new SpawnGroup.SpawnPoint(925, 1133, 0),
            new SpawnGroup.SpawnPoint(992, 1140, 0),
            new SpawnGroup.SpawnPoint(1120, 1172, 0),
            new SpawnGroup.SpawnPoint(975, 1192, 0),
            new SpawnGroup.SpawnPoint(1136, 1269, 0)));
  }
}
