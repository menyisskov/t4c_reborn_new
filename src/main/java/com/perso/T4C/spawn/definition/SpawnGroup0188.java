package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0188 {
  private SpawnGroup0188() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Haden NPC",
        800,
        1600,
        1,
        5,
        List.of("HADEN"),
        List.of(new SpawnGroup.SpawnPoint(87, 2654, 0)));
  }
}
