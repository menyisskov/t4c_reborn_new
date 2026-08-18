package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0127 {
  private SpawnGroup0127() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Mercenary A",
        15,
        30,
        2,
        5,
        List.of("MOBMERCENARYA"),
        List.of(
            new SpawnGroup.SpawnPoint(2678, 1059, 0),
            new SpawnGroup.SpawnPoint(2694, 1062, 0),
            new SpawnGroup.SpawnPoint(2682, 1066, 0),
            new SpawnGroup.SpawnPoint(2688, 1066, 0),
            new SpawnGroup.SpawnPoint(2672, 1068, 0),
            new SpawnGroup.SpawnPoint(2664, 1072, 0),
            new SpawnGroup.SpawnPoint(2677, 1072, 0),
            new SpawnGroup.SpawnPoint(2669, 1082, 0)));
  }
}
