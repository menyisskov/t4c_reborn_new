package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0035 {
  private SpawnGroup0035() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Shadow Demon",
        1300,
        1600,
        1,
        5,
        List.of("Shadow Demon"),
        List.of(new SpawnGroup.SpawnPoint(1438, 2730, 2)));
  }
}
