package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0259 {
  private SpawnGroup0259() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Terror Demon",
        30,
        60,
        1,
        5,
        List.of("Terror Demon"),
        List.of(new SpawnGroup.SpawnPoint(1477, 1793, 1)));
  }
}
