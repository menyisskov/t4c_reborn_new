package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0025 {
  private SpawnGroup0025() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Great Wolf",
        30,
        60,
        1,
        5,
        List.of("Great Wolf"),
        List.of(
            new SpawnGroup.SpawnPoint(354, 379, 0),
            new SpawnGroup.SpawnPoint(809, 519, 0),
            new SpawnGroup.SpawnPoint(826, 532, 0),
            new SpawnGroup.SpawnPoint(824, 538, 0),
            new SpawnGroup.SpawnPoint(792, 541, 0),
            new SpawnGroup.SpawnPoint(797, 546, 0),
            new SpawnGroup.SpawnPoint(811, 548, 0),
            new SpawnGroup.SpawnPoint(95, 559, 0),
            new SpawnGroup.SpawnPoint(87, 606, 0),
            new SpawnGroup.SpawnPoint(833, 855, 0),
            new SpawnGroup.SpawnPoint(555, 860, 0),
            new SpawnGroup.SpawnPoint(818, 862, 0),
            new SpawnGroup.SpawnPoint(809, 873, 0),
            new SpawnGroup.SpawnPoint(439, 935, 0),
            new SpawnGroup.SpawnPoint(724, 1029, 0),
            new SpawnGroup.SpawnPoint(574, 1068, 0),
            new SpawnGroup.SpawnPoint(772, 1074, 0),
            new SpawnGroup.SpawnPoint(554, 1076, 0),
            new SpawnGroup.SpawnPoint(577, 1076, 0),
            new SpawnGroup.SpawnPoint(581, 1084, 0),
            new SpawnGroup.SpawnPoint(697, 1089, 0),
            new SpawnGroup.SpawnPoint(557, 1091, 0),
            new SpawnGroup.SpawnPoint(570, 1092, 0)));
  }
}
