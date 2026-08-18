package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0141 {
  private SpawnGroup0141() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Stinkbreath",
        1100,
        1300,
        1,
        7,
        List.of("Stinkbreath"),
        List.of(new SpawnGroup.SpawnPoint(941, 1048, 2)));
  }
}
