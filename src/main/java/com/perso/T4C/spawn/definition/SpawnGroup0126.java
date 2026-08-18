package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0126 {
  private SpawnGroup0126() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Undead Bat",
        25,
        50,
        2,
        6,
        List.of("Undead Bat"),
        List.of(
            new SpawnGroup.SpawnPoint(300, 21, 1),
            new SpawnGroup.SpawnPoint(292, 31, 1),
            new SpawnGroup.SpawnPoint(331, 52, 1),
            new SpawnGroup.SpawnPoint(273, 55, 1),
            new SpawnGroup.SpawnPoint(240, 71, 1),
            new SpawnGroup.SpawnPoint(199, 101, 1),
            new SpawnGroup.SpawnPoint(35, 281, 1),
            new SpawnGroup.SpawnPoint(14, 306, 1),
            new SpawnGroup.SpawnPoint(9, 335, 1)));
  }
}
