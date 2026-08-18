package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0121 {
  private SpawnGroup0121() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Jarko",
        1200,
        1500,
        1,
        6,
        List.of("JARKO"),
        List.of(new SpawnGroup.SpawnPoint(1088, 103, 1)));
  }
}
