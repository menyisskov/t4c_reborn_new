package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0115 {
  private SpawnGroup0115() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Goblin Warlord",
        500,
        900,
        1,
        6,
        List.of("Goblin Warlord", "Goblin Chieftain"),
        List.of(
            new SpawnGroup.SpawnPoint(1949, 134, 0),
            new SpawnGroup.SpawnPoint(1796, 159, 0),
            new SpawnGroup.SpawnPoint(2309, 163, 0),
            new SpawnGroup.SpawnPoint(2022, 164, 0),
            new SpawnGroup.SpawnPoint(2140, 279, 0),
            new SpawnGroup.SpawnPoint(1888, 298, 0),
            new SpawnGroup.SpawnPoint(1896, 298, 0),
            new SpawnGroup.SpawnPoint(1892, 301, 0),
            new SpawnGroup.SpawnPoint(1969, 313, 0),
            new SpawnGroup.SpawnPoint(1741, 327, 0),
            new SpawnGroup.SpawnPoint(2070, 337, 0),
            new SpawnGroup.SpawnPoint(2221, 418, 0),
            new SpawnGroup.SpawnPoint(1796, 439, 0),
            new SpawnGroup.SpawnPoint(1837, 541, 0),
            new SpawnGroup.SpawnPoint(1054, 93, 1),
            new SpawnGroup.SpawnPoint(1042, 108, 1),
            new SpawnGroup.SpawnPoint(1023, 125, 1),
            new SpawnGroup.SpawnPoint(1027, 144, 1),
            new SpawnGroup.SpawnPoint(1023, 148, 1),
            new SpawnGroup.SpawnPoint(981, 199, 1),
            new SpawnGroup.SpawnPoint(1033, 209, 1),
            new SpawnGroup.SpawnPoint(1153, 210, 1)));
  }
}
