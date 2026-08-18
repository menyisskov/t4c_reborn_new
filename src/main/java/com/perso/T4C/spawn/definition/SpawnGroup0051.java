package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0051 {
  private SpawnGroup0051() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Mortal Wombat",
        800,
        1300,
        1,
        5,
        List.of("Mortal Wombat"),
        List.of(new SpawnGroup.SpawnPoint(1542, 372, 2)));
  }
}
