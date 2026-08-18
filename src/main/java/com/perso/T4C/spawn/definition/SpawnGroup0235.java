package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0235 {
  private SpawnGroup0235() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Gorlok Bloodaxe",
        150,
        180,
        1,
        5,
        List.of("GORLOKBLOODAXE"),
        List.of(new SpawnGroup.SpawnPoint(2605, 1200, 0)));
  }
}
