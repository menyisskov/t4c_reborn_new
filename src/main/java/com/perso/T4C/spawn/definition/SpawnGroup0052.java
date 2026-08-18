package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0052 {
  private SpawnGroup0052() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Kahp Leth Guard 1",
        500,
        1000,
        1,
        1,
        List.of("KAHPLETHGUARD1"),
        List.of(
            new SpawnGroup.SpawnPoint(1980, 1269, 2), new SpawnGroup.SpawnPoint(1970, 1278, 2)));
  }
}
