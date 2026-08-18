package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0252 {
  private SpawnGroup0252() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Defiler",
        30,
        60,
        3,
        5,
        List.of("Defiler"),
        List.of(
            new SpawnGroup.SpawnPoint(1028, 1841, 1),
            new SpawnGroup.SpawnPoint(1020, 1844, 1),
            new SpawnGroup.SpawnPoint(1035, 1848, 1),
            new SpawnGroup.SpawnPoint(1019, 1852, 1),
            new SpawnGroup.SpawnPoint(1172, 1896, 1),
            new SpawnGroup.SpawnPoint(1169, 1900, 1),
            new SpawnGroup.SpawnPoint(905, 1909, 1),
            new SpawnGroup.SpawnPoint(883, 1911, 1),
            new SpawnGroup.SpawnPoint(884, 1935, 1),
            new SpawnGroup.SpawnPoint(819, 1937, 1),
            new SpawnGroup.SpawnPoint(855, 1947, 1),
            new SpawnGroup.SpawnPoint(912, 1948, 1),
            new SpawnGroup.SpawnPoint(869, 1964, 1),
            new SpawnGroup.SpawnPoint(1044, 1967, 1),
            new SpawnGroup.SpawnPoint(1036, 1973, 1),
            new SpawnGroup.SpawnPoint(1038, 1981, 1),
            new SpawnGroup.SpawnPoint(942, 1983, 1),
            new SpawnGroup.SpawnPoint(865, 1984, 1),
            new SpawnGroup.SpawnPoint(915, 1993, 1),
            new SpawnGroup.SpawnPoint(994, 1998, 1),
            new SpawnGroup.SpawnPoint(951, 2003, 1),
            new SpawnGroup.SpawnPoint(1099, 2038, 1),
            new SpawnGroup.SpawnPoint(1103, 2040, 1),
            new SpawnGroup.SpawnPoint(1096, 2045, 1)));
  }
}
