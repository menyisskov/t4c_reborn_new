package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0189 {
  private SpawnGroup0189() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Grimish NPC",
        1000,
        1100,
        1,
        5,
        List.of("GRIMISH"),
        List.of(new SpawnGroup.SpawnPoint(1400, 1827, 0)));
  }
}
