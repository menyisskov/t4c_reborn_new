package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0197 {
  private SpawnGroup0197() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Iago Caballero",
        1800,
        3600,
        1,
        5,
        List.of("IAGOCABALLERO"),
        List.of(new SpawnGroup.SpawnPoint(812, 2714, 0)));
  }
}
