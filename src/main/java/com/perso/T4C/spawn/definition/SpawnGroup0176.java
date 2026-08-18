package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0176 {
  private SpawnGroup0176() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Skrull",
        1100,
        1300,
        1,
        5,
        List.of("Skrull"),
        List.of(new SpawnGroup.SpawnPoint(949, 1704, 0)));
  }
}
