package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0050 {
  private SpawnGroup0050() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Deathstalker",
        800,
        1300,
        1,
        5,
        List.of("Deathstalker"),
        List.of(new SpawnGroup.SpawnPoint(1638, 498, 2)));
  }
}
