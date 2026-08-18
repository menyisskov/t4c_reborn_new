package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0014 {
  private SpawnGroup0014() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Centaur Avenger",
        40,
        60,
        1,
        5,
        List.of("CENTAURAVENGER"),
        List.of(
            new SpawnGroup.SpawnPoint(915, 822, 0),
            new SpawnGroup.SpawnPoint(947, 828, 0),
            new SpawnGroup.SpawnPoint(929, 841, 0),
            new SpawnGroup.SpawnPoint(956, 855, 0),
            new SpawnGroup.SpawnPoint(882, 928, 0),
            new SpawnGroup.SpawnPoint(833, 985, 0),
            new SpawnGroup.SpawnPoint(934, 1071, 0),
            new SpawnGroup.SpawnPoint(1189, 1077, 0),
            new SpawnGroup.SpawnPoint(1196, 1087, 0),
            new SpawnGroup.SpawnPoint(1087, 1111, 0),
            new SpawnGroup.SpawnPoint(1100, 1114, 0),
            new SpawnGroup.SpawnPoint(894, 1117, 0),
            new SpawnGroup.SpawnPoint(1067, 1257, 0),
            new SpawnGroup.SpawnPoint(1064, 1265, 0)));
  }
}
