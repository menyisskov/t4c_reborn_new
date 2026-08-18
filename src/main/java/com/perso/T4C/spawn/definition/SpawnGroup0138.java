package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0138 {
  private SpawnGroup0138() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Gorben",
        1000,
        1300,
        1,
        5,
        List.of("Gorben"),
        List.of(new SpawnGroup.SpawnPoint(2010, 429, 0)));
  }
}
