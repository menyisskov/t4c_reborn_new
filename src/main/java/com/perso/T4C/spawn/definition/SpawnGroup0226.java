package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0226 {
  private SpawnGroup0226() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Hunter2",
        300,
        600,
        1,
        5,
        List.of("MOBHUNTER2"),
        List.of(new SpawnGroup.SpawnPoint(780, 439, 0), new SpawnGroup.SpawnPoint(792, 801, 0)));
  }
}
