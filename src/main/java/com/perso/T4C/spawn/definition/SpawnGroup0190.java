package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0190 {
  private SpawnGroup0190() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Ghundarg Rumblefoot",
        1000,
        1100,
        1,
        5,
        List.of("GHUNDARGRUMBLEFOOT"),
        List.of(new SpawnGroup.SpawnPoint(1135, 1798, 0)));
  }
}
