package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0097 {
  private SpawnGroup0097() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Crypt",
        110,
        150,
        2,
        5,
        List.of("Decaying Zombie", "Mummy"),
        List.of(
            new SpawnGroup.SpawnPoint(548, 230, 1),
            new SpawnGroup.SpawnPoint(416, 235, 1),
            new SpawnGroup.SpawnPoint(535, 238, 1),
            new SpawnGroup.SpawnPoint(428, 248, 1),
            new SpawnGroup.SpawnPoint(534, 258, 1),
            new SpawnGroup.SpawnPoint(509, 264, 1),
            new SpawnGroup.SpawnPoint(528, 267, 1),
            new SpawnGroup.SpawnPoint(486, 290, 1),
            new SpawnGroup.SpawnPoint(527, 294, 1),
            new SpawnGroup.SpawnPoint(35, 447, 1),
            new SpawnGroup.SpawnPoint(38, 468, 1),
            new SpawnGroup.SpawnPoint(72, 600, 1),
            new SpawnGroup.SpawnPoint(102, 603, 1),
            new SpawnGroup.SpawnPoint(69, 606, 1),
            new SpawnGroup.SpawnPoint(85, 606, 1),
            new SpawnGroup.SpawnPoint(67, 611, 1),
            new SpawnGroup.SpawnPoint(68, 611, 1),
            new SpawnGroup.SpawnPoint(88, 627, 1),
            new SpawnGroup.SpawnPoint(49, 631, 1),
            new SpawnGroup.SpawnPoint(78, 640, 1),
            new SpawnGroup.SpawnPoint(61, 644, 1),
            new SpawnGroup.SpawnPoint(64, 645, 1),
            new SpawnGroup.SpawnPoint(65, 645, 1),
            new SpawnGroup.SpawnPoint(97, 645, 1),
            new SpawnGroup.SpawnPoint(81, 650, 1),
            new SpawnGroup.SpawnPoint(82, 650, 1),
            new SpawnGroup.SpawnPoint(131, 672, 1)));
  }
}
