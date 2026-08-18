package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0205 {
  private SpawnGroup0205() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Trish Yellowblood",
        800,
        1300,
        1,
        5,
        List.of("Trish Yellowblood"),
        List.of(new SpawnGroup.SpawnPoint(863, 2615, 2)));
  }
}
