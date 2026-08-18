package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0186 {
  private SpawnGroup0186() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "MOB Obsidian Knight",
        15,
        40,
        2,
        5,
        List.of("MOBOBSIDIANCONCLAVEKNIGHT"),
        List.of(
            new SpawnGroup.SpawnPoint(567, 764, 0),
            new SpawnGroup.SpawnPoint(579, 768, 0),
            new SpawnGroup.SpawnPoint(568, 771, 0),
            new SpawnGroup.SpawnPoint(573, 771, 0)));
  }
}
