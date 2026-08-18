package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0213 {
  private SpawnGroup0213() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Blaarg Toemangler",
        800,
        1300,
        1,
        5,
        List.of("Blaarg Toemangler"),
        List.of(new SpawnGroup.SpawnPoint(931, 1042, 2)));
  }
}
