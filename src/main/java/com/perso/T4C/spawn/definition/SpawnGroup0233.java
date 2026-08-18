package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0233 {
  private SpawnGroup0233() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Olin Haad Commander",
        60,
        120,
        1,
        1,
        List.of("OLINHAADCOMMANDER"),
        List.of(new SpawnGroup.SpawnPoint(2973, 246, 0)));
  }
}
