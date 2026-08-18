package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0077 {
  private SpawnGroup0077() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Flipped Wooden Door",
        1,
        1,
        1,
        0,
        List.of("ORACLEESCAPEFLIPPEDDOOR"),
        List.of(
            new SpawnGroup.SpawnPoint(2653, 2407, 2),
            new SpawnGroup.SpawnPoint(2669, 2423, 2),
            new SpawnGroup.SpawnPoint(2621, 2439, 2),
            new SpawnGroup.SpawnPoint(2637, 2455, 2)));
  }
}
