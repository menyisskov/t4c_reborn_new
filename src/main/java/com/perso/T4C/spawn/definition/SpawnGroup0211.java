package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0211 {
  private SpawnGroup0211() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Grudish Earchewer",
        800,
        1300,
        1,
        5,
        List.of("Grudish Earchewer"),
        List.of(new SpawnGroup.SpawnPoint(864, 1196, 2)));
  }
}
