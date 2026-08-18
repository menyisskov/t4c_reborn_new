package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0134 {
  private SpawnGroup0134() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Fenrir",
        600,
        600,
        1,
        1,
        List.of("FENRIR"),
        List.of(new SpawnGroup.SpawnPoint(157, 211, 0)));
  }
}
