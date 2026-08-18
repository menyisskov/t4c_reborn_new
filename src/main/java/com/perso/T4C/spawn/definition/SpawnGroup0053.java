package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0053 {
  private SpawnGroup0053() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Kahp Leth Guard 2",
        500,
        1000,
        1,
        1,
        List.of("KAHPLETHGUARD2"),
        List.of(
            new SpawnGroup.SpawnPoint(2789, 539, 2),
            new SpawnGroup.SpawnPoint(2771, 557, 2),
            new SpawnGroup.SpawnPoint(2799, 558, 2),
            new SpawnGroup.SpawnPoint(2790, 567, 2),
            new SpawnGroup.SpawnPoint(2819, 569, 2),
            new SpawnGroup.SpawnPoint(2801, 587, 2)));
  }
}
