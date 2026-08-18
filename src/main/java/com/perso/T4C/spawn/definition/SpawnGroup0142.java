package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0142 {
  private SpawnGroup0142() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Deep One Boss",
        1100,
        1500,
        1,
        6,
        List.of("DEEPONEBOSS"),
        List.of(new SpawnGroup.SpawnPoint(932, 424, 2)));
  }
}
