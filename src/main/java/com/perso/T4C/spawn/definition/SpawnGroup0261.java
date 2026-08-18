package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0261 {
  private SpawnGroup0261() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Darkness Demon",
        30,
        60,
        1,
        5,
        List.of("Darkness Demon"),
        List.of(new SpawnGroup.SpawnPoint(1475, 1514, 1)));
  }
}
