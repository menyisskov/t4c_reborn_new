package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0198 {
  private SpawnGroup0198() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Grey Leaf",
        800,
        1100,
        1,
        6,
        List.of("GREYLEAF"),
        List.of(new SpawnGroup.SpawnPoint(849, 2086, 0)));
  }
}
