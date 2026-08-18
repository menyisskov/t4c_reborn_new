package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0191 {
  private SpawnGroup0191() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Zhakar",
        1200,
        1250,
        1,
        5,
        List.of("ZHAKAR"),
        List.of(new SpawnGroup.SpawnPoint(55, 1769, 0)));
  }
}
