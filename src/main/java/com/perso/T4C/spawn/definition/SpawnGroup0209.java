package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0209 {
  private SpawnGroup0209() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Eye-Patched Qardos",
        800,
        1300,
        1,
        5,
        List.of("Eye-Patched Qardos"),
        List.of(new SpawnGroup.SpawnPoint(879, 2441, 0)));
  }
}
