package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0089 {
  private SpawnGroup0089() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Undead Guardian",
        120,
        360,
        2,
        0,
        List.of("Undead Guardian"),
        List.of(
            new SpawnGroup.SpawnPoint(2265, 195, 1),
            new SpawnGroup.SpawnPoint(2165, 295, 1),
            new SpawnGroup.SpawnPoint(2365, 295, 1),
            new SpawnGroup.SpawnPoint(2265, 395, 1),
            new SpawnGroup.SpawnPoint(2800, 503, 1),
            new SpawnGroup.SpawnPoint(2678, 519, 1),
            new SpawnGroup.SpawnPoint(2862, 561, 1),
            new SpawnGroup.SpawnPoint(2585, 613, 1),
            new SpawnGroup.SpawnPoint(2922, 621, 1),
            new SpawnGroup.SpawnPoint(2920, 681, 1),
            new SpawnGroup.SpawnPoint(2586, 686, 1),
            new SpawnGroup.SpawnPoint(2869, 729, 1),
            new SpawnGroup.SpawnPoint(2668, 766, 1),
            new SpawnGroup.SpawnPoint(2807, 791, 1),
            new SpawnGroup.SpawnPoint(2732, 832, 1)));
  }
}
