package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0124 {
  private SpawnGroup0124() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Wizard & Guardian",
        20,
        30,
        2,
        5,
        List.of("Wizard Mummy", "Skeleton Guardian"),
        List.of(
            new SpawnGroup.SpawnPoint(447, 166, 1),
            new SpawnGroup.SpawnPoint(456, 180, 1),
            new SpawnGroup.SpawnPoint(428, 183, 1),
            new SpawnGroup.SpawnPoint(485, 191, 1),
            new SpawnGroup.SpawnPoint(400, 197, 1),
            new SpawnGroup.SpawnPoint(394, 208, 1),
            new SpawnGroup.SpawnPoint(448, 226, 1),
            new SpawnGroup.SpawnPoint(446, 250, 1),
            new SpawnGroup.SpawnPoint(464, 266, 1),
            new SpawnGroup.SpawnPoint(475, 283, 1),
            new SpawnGroup.SpawnPoint(518, 299, 1),
            new SpawnGroup.SpawnPoint(493, 314, 1),
            new SpawnGroup.SpawnPoint(1275, 2172, 2),
            new SpawnGroup.SpawnPoint(1274, 2211, 2),
            new SpawnGroup.SpawnPoint(1301, 2236, 2),
            new SpawnGroup.SpawnPoint(1245, 2273, 2),
            new SpawnGroup.SpawnPoint(1312, 2292, 2),
            new SpawnGroup.SpawnPoint(1232, 2337, 2),
            new SpawnGroup.SpawnPoint(1211, 2371, 2),
            new SpawnGroup.SpawnPoint(874, 2543, 2),
            new SpawnGroup.SpawnPoint(892, 2566, 2),
            new SpawnGroup.SpawnPoint(867, 2570, 2),
            new SpawnGroup.SpawnPoint(704, 2584, 2),
            new SpawnGroup.SpawnPoint(712, 2596, 2),
            new SpawnGroup.SpawnPoint(704, 2615, 2),
            new SpawnGroup.SpawnPoint(797, 2617, 2)));
  }
}
