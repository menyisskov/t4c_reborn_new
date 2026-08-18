package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0082 {
  private SpawnGroup0082() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Doktor Spine",
        800,
        1200,
        1,
        5,
        List.of("DOKTORSPINE"),
        List.of(new SpawnGroup.SpawnPoint(2831, 2331, 0)));
  }
}
