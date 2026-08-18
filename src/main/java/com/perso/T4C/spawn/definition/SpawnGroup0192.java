package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0192 {
  private SpawnGroup0192() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Brownbark",
        1200,
        1250,
        1,
        5,
        List.of("AARONBROWNBARK"),
        List.of(new SpawnGroup.SpawnPoint(812, 1568, 0)));
  }
}
