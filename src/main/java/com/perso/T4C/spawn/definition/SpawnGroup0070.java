package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0070 {
  private SpawnGroup0070() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Vulnerable Guardian",
        60,
        120,
        1,
        0,
        List.of("ORACLEVULNERABLEGUARDIAN"),
        List.of(
            new SpawnGroup.SpawnPoint(2802, 2190, 2), new SpawnGroup.SpawnPoint(2722, 2270, 2)));
  }
}
