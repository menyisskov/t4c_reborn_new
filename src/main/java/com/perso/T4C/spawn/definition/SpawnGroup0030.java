package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0030 {
  private SpawnGroup0030() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Skeletal Centaur",
        100,
        300,
        1,
        5,
        List.of("Skeletal Centaur"),
        List.of(
            new SpawnGroup.SpawnPoint(504, 426, 1),
            new SpawnGroup.SpawnPoint(496, 447, 1),
            new SpawnGroup.SpawnPoint(512, 453, 1),
            new SpawnGroup.SpawnPoint(459, 483, 1),
            new SpawnGroup.SpawnPoint(563, 490, 1),
            new SpawnGroup.SpawnPoint(552, 497, 1),
            new SpawnGroup.SpawnPoint(423, 503, 1),
            new SpawnGroup.SpawnPoint(438, 508, 1),
            new SpawnGroup.SpawnPoint(569, 537, 1),
            new SpawnGroup.SpawnPoint(577, 549, 1),
            new SpawnGroup.SpawnPoint(427, 552, 1),
            new SpawnGroup.SpawnPoint(449, 556, 1),
            new SpawnGroup.SpawnPoint(550, 558, 1),
            new SpawnGroup.SpawnPoint(526, 578, 1),
            new SpawnGroup.SpawnPoint(476, 580, 1),
            new SpawnGroup.SpawnPoint(503, 599, 1),
            new SpawnGroup.SpawnPoint(481, 604, 1)));
  }
}
