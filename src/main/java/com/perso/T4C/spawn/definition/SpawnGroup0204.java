package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0204 {
  private SpawnGroup0204() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Mrish Yellowblood",
        800,
        1300,
        1,
        5,
        List.of("Mrish Yellowblood"),
        List.of(new SpawnGroup.SpawnPoint(864, 2421, 2)));
  }
}
