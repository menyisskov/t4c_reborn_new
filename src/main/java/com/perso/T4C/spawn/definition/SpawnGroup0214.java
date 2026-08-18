package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0214 {
  private SpawnGroup0214() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Bend Hayjes the rotten",
        800,
        1300,
        1,
        5,
        List.of("Bend Hayjes"),
        List.of(new SpawnGroup.SpawnPoint(456, 1909, 1)));
  }
}
