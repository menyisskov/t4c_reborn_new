package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0262 {
  private SpawnGroup0262() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Vicar Asgoth",
        1100,
        1400,
        1,
        1,
        List.of("VICARASGOTH"),
        List.of(new SpawnGroup.SpawnPoint(1004, 2278, 1)));
  }
}
