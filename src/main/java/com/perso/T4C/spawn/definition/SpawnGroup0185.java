package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0185 {
  private SpawnGroup0185() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Headsmasher",
        1100,
        1100,
        1,
        5,
        List.of("Headsmasher"),
        List.of(new SpawnGroup.SpawnPoint(338, 623, 1)));
  }
}
