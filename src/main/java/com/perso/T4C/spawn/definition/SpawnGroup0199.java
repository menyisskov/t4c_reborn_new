package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0199 {
  private SpawnGroup0199() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Shadeen",
        900,
        1210,
        1,
        5,
        List.of("SHADEEN"),
        List.of(new SpawnGroup.SpawnPoint(1593, 102, 1)));
  }
}
