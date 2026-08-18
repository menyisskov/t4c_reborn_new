package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0045 {
  private SpawnGroup0045() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Toll troll",
        45,
        60,
        1,
        5,
        List.of("Toll Troll"),
        List.of(
            new SpawnGroup.SpawnPoint(1905, 2711, 0),
            new SpawnGroup.SpawnPoint(1912, 2718, 0),
            new SpawnGroup.SpawnPoint(2017, 2718, 0),
            new SpawnGroup.SpawnPoint(2018, 2719, 0),
            new SpawnGroup.SpawnPoint(1907, 2723, 0),
            new SpawnGroup.SpawnPoint(1919, 2725, 0),
            new SpawnGroup.SpawnPoint(1914, 2729, 0),
            new SpawnGroup.SpawnPoint(1914, 2730, 0),
            new SpawnGroup.SpawnPoint(1979, 2741, 0),
            new SpawnGroup.SpawnPoint(2043, 2744, 0),
            new SpawnGroup.SpawnPoint(1985, 2747, 0),
            new SpawnGroup.SpawnPoint(2052, 2753, 0),
            new SpawnGroup.SpawnPoint(2019, 2781, 0),
            new SpawnGroup.SpawnPoint(2024, 2786, 0),
            new SpawnGroup.SpawnPoint(2029, 2791, 0)));
  }
}
