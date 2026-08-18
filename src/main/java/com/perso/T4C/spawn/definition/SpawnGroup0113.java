package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0113 {
  private SpawnGroup0113() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Ooze",
        18,
        30,
        2,
        6,
        List.of("Organic Waste"),
        List.of(
            new SpawnGroup.SpawnPoint(439, 426, 2),
            new SpawnGroup.SpawnPoint(432, 441, 2),
            new SpawnGroup.SpawnPoint(419, 462, 2),
            new SpawnGroup.SpawnPoint(215, 482, 2),
            new SpawnGroup.SpawnPoint(173, 509, 2),
            new SpawnGroup.SpawnPoint(195, 527, 2),
            new SpawnGroup.SpawnPoint(202, 696, 2)));
  }
}
