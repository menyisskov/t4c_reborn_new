package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0268 {
  private SpawnGroup0268() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Olin Haad",
        1000,
        1400,
        1,
        1,
        List.of("OLINHAAD3"),
        List.of(new SpawnGroup.SpawnPoint(1776, 2380, 1)));
  }
}
