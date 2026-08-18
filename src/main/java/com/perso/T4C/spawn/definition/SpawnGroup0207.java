package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0207 {
  private SpawnGroup0207() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Roshnak Tul",
        800,
        1300,
        1,
        5,
        List.of("Roshnak Tul"),
        List.of(new SpawnGroup.SpawnPoint(2652, 531, 0)));
  }
}
