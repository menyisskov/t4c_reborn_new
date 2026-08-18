package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0208 {
  private SpawnGroup0208() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Bugar Pouchsnatcher",
        800,
        1300,
        1,
        5,
        List.of("Bugar Pouchsnatcher"),
        List.of(new SpawnGroup.SpawnPoint(1232, 2256, 0)));
  }
}
