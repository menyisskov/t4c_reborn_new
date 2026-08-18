package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0122 {
  private SpawnGroup0122() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Mworgwloth the Troll",
        1200,
        1500,
        1,
        6,
        List.of("MHORGWLOTH"),
        List.of(new SpawnGroup.SpawnPoint(1566, 174, 0)));
  }
}
