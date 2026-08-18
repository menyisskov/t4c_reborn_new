package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0201 {
  private SpawnGroup0201() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Guurk",
        800,
        1300,
        1,
        5,
        List.of("Guurk"),
        List.of(new SpawnGroup.SpawnPoint(210, 621, 2)));
  }
}
