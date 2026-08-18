package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0133 {
  private SpawnGroup0133() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Jormungand",
        600,
        600,
        1,
        1,
        List.of("JORMUNGAND"),
        List.of(new SpawnGroup.SpawnPoint(480, 2008, 0)));
  }
}
