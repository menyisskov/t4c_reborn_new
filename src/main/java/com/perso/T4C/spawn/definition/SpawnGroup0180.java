package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0180 {
  private SpawnGroup0180() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Dead Bolt",
        1100,
        1300,
        1,
        6,
        List.of("Dead Bolt", "Dead Bolt"),
        List.of(new SpawnGroup.SpawnPoint(851, 1707, 2)));
  }
}
