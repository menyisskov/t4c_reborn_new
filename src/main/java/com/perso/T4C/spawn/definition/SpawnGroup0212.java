package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0212 {
  private SpawnGroup0212() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Liedric Throatcutter",
        800,
        1300,
        1,
        5,
        List.of("Liedric Throatcutter"),
        List.of(new SpawnGroup.SpawnPoint(495, 1624, 0)));
  }
}
