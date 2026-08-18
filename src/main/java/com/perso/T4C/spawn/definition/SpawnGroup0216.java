package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0216 {
  private SpawnGroup0216() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Hurbag Nailripper",
        800,
        1300,
        1,
        5,
        List.of("Hurbag Nailripper"),
        List.of(new SpawnGroup.SpawnPoint(1257, 1811, 0)));
  }
}
