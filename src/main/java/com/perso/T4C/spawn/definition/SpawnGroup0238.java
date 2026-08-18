package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0238 {
  private SpawnGroup0238() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Lighthaven Ranger",
        30,
        60,
        1,
        5,
        List.of("Lighthaven Ranger"),
        List.of(
            new SpawnGroup.SpawnPoint(2777, 1201, 0),
            new SpawnGroup.SpawnPoint(2783, 1203, 0),
            new SpawnGroup.SpawnPoint(2780, 1209, 0)));
  }
}
