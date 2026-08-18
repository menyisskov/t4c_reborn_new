package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0096 {
  private SpawnGroup0096() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Cemetary",
        120,
        200,
        1,
        5,
        List.of("Skeleton", "Decaying Zombie"),
        List.of(
            new SpawnGroup.SpawnPoint(2782, 1071, 0),
            new SpawnGroup.SpawnPoint(2790, 1082, 0),
            new SpawnGroup.SpawnPoint(2772, 1086, 0),
            new SpawnGroup.SpawnPoint(2767, 1087, 0),
            new SpawnGroup.SpawnPoint(2798, 1100, 0),
            new SpawnGroup.SpawnPoint(2785, 1106, 0),
            new SpawnGroup.SpawnPoint(2781, 1115, 0),
            new SpawnGroup.SpawnPoint(469, 140, 1),
            new SpawnGroup.SpawnPoint(464, 154, 1),
            new SpawnGroup.SpawnPoint(478, 161, 1),
            new SpawnGroup.SpawnPoint(2812, 290, 1),
            new SpawnGroup.SpawnPoint(34, 447, 1),
            new SpawnGroup.SpawnPoint(50, 461, 1),
            new SpawnGroup.SpawnPoint(57, 488, 1),
            new SpawnGroup.SpawnPoint(63, 523, 1),
            new SpawnGroup.SpawnPoint(84, 606, 1),
            new SpawnGroup.SpawnPoint(89, 609, 1),
            new SpawnGroup.SpawnPoint(100, 620, 1),
            new SpawnGroup.SpawnPoint(59, 627, 1),
            new SpawnGroup.SpawnPoint(103, 634, 1),
            new SpawnGroup.SpawnPoint(86, 635, 1),
            new SpawnGroup.SpawnPoint(69, 639, 1),
            new SpawnGroup.SpawnPoint(113, 682, 1)));
  }
}
