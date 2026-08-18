package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0229 {
  private SpawnGroup0229() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "ExitGate",
        1,
        1,
        1,
        0,
        List.of("EXITGATE"),
        List.of(
            new SpawnGroup.SpawnPoint(1832, 1723, 0),
            new SpawnGroup.SpawnPoint(1749, 1810, 0),
            new SpawnGroup.SpawnPoint(2011, 1833, 0),
            new SpawnGroup.SpawnPoint(2005, 1853, 0),
            new SpawnGroup.SpawnPoint(1659, 2006, 0)));
  }
}
