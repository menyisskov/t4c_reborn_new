package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0135 {
  private SpawnGroup0135() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Bane Blackblood",
        1500,
        2500,
        1,
        5,
        List.of("BANEBLACKBLOOD"),
        List.of(new SpawnGroup.SpawnPoint(342, 1679, 0)));
  }
}
