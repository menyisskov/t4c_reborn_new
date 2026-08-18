package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0202 {
  private SpawnGroup0202() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Scar-face Razek",
        800,
        1300,
        1,
        5,
        List.of("Scar-Face Razek"),
        List.of(new SpawnGroup.SpawnPoint(2242, 1498, 0)));
  }
}
