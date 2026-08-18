package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0203 {
  private SpawnGroup0203() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Ruk the miner",
        800,
        1300,
        1,
        5,
        List.of("Ruk"),
        List.of(new SpawnGroup.SpawnPoint(193, 2639, 2)));
  }
}
