package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0027 {
  private SpawnGroup0027() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Fenris Wolf",
        200,
        400,
        1,
        5,
        List.of("Fenris Wolf"),
        List.of(
            new SpawnGroup.SpawnPoint(811, 536, 0),
            new SpawnGroup.SpawnPoint(826, 864, 0),
            new SpawnGroup.SpawnPoint(570, 1079, 0)));
  }
}
