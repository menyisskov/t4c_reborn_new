package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0085 {
  private SpawnGroup0085() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Crazed Nurse",
        40,
        80,
        2,
        5,
        List.of("MOBCRAZEDNURSE"),
        List.of(
            new SpawnGroup.SpawnPoint(2811, 2206, 0),
            new SpawnGroup.SpawnPoint(2808, 2220, 0),
            new SpawnGroup.SpawnPoint(2824, 2222, 0),
            new SpawnGroup.SpawnPoint(2831, 2234, 0),
            new SpawnGroup.SpawnPoint(2811, 2240, 0),
            new SpawnGroup.SpawnPoint(2815, 2251, 0),
            new SpawnGroup.SpawnPoint(2732, 2296, 0),
            new SpawnGroup.SpawnPoint(2707, 2307, 0),
            new SpawnGroup.SpawnPoint(2827, 2309, 0),
            new SpawnGroup.SpawnPoint(2832, 2319, 0),
            new SpawnGroup.SpawnPoint(2827, 2391, 0),
            new SpawnGroup.SpawnPoint(2746, 2407, 0),
            new SpawnGroup.SpawnPoint(2830, 2408, 0),
            new SpawnGroup.SpawnPoint(2720, 2472, 0),
            new SpawnGroup.SpawnPoint(2744, 2483, 0),
            new SpawnGroup.SpawnPoint(2714, 2492, 0),
            new SpawnGroup.SpawnPoint(2746, 2494, 0),
            new SpawnGroup.SpawnPoint(2729, 2555, 0),
            new SpawnGroup.SpawnPoint(2727, 2568, 0),
            new SpawnGroup.SpawnPoint(2743, 2569, 0),
            new SpawnGroup.SpawnPoint(2726, 2576, 0),
            new SpawnGroup.SpawnPoint(2749, 2581, 0),
            new SpawnGroup.SpawnPoint(2736, 2590, 0)));
  }
}
