package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0224 {
  private SpawnGroup0224() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Hunter1",
        300,
        600,
        1,
        5,
        List.of("MOBHUNTER1"),
        List.of(
            new SpawnGroup.SpawnPoint(495, 553, 0),
            new SpawnGroup.SpawnPoint(437, 755, 0),
            new SpawnGroup.SpawnPoint(607, 925, 0)));
  }
}
