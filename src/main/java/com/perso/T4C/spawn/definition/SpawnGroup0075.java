package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0075 {
  private SpawnGroup0075() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Gabriel Archonis",
        8,
        8,
        1,
        0,
        List.of("GABRIELARCHONIS"),
        List.of(new SpawnGroup.SpawnPoint(2628, 2448, 2)));
  }
}
