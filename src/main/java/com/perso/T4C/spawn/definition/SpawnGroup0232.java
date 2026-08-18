package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0232 {
  private SpawnGroup0232() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Olin Haad Soldiers",
        30,
        60,
        1,
        1,
        List.of("Olin Haad Soldier 10", "Olin Haad Soldier 12"),
        List.of(
            new SpawnGroup.SpawnPoint(2973, 239, 0),
            new SpawnGroup.SpawnPoint(2965, 241, 0),
            new SpawnGroup.SpawnPoint(2980, 241, 0),
            new SpawnGroup.SpawnPoint(2982, 248, 0),
            new SpawnGroup.SpawnPoint(2967, 250, 0),
            new SpawnGroup.SpawnPoint(2976, 253, 0)));
  }
}
