package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0139 {
  private SpawnGroup0139() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Green Slime",
        20,
        40,
        1,
        8,
        List.of("Green Slime"),
        List.of(new SpawnGroup.SpawnPoint(400, 402, 1), new SpawnGroup.SpawnPoint(410, 408, 1)));
  }
}
