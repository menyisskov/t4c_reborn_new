package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0076 {
  private SpawnGroup0076() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Wooden Door",
        1,
        1,
        1,
        0,
        List.of("ORACLEESCAPEDOOR"),
        List.of(
            new SpawnGroup.SpawnPoint(2669, 2407, 2),
            new SpawnGroup.SpawnPoint(2653, 2423, 2),
            new SpawnGroup.SpawnPoint(2637, 2439, 2),
            new SpawnGroup.SpawnPoint(2621, 2455, 2)));
  }
}
