package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0128 {
  private SpawnGroup0128() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Araf Kul",
        800,
        1300,
        1,
        8,
        List.of("Araf Kul"),
        List.of(new SpawnGroup.SpawnPoint(2654, 565, 0)));
  }
}
