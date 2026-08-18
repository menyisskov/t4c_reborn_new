package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0083 {
  private SpawnGroup0083() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Mad Madrigan",
        1800,
        3600,
        1,
        5,
        List.of("MADMADRIGAN"),
        List.of(new SpawnGroup.SpawnPoint(2810, 2572, 0)));
  }
}
