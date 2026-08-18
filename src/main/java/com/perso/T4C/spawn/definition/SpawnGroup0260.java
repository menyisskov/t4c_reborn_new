package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0260 {
  private SpawnGroup0260() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Chaos Demon",
        30,
        60,
        1,
        5,
        List.of("Chaos Demon"),
        List.of(new SpawnGroup.SpawnPoint(1755, 1794, 1)));
  }
}
