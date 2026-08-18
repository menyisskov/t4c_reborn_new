package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0040 {
  private SpawnGroup0040() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Giant Black Widow",
        300,
        600,
        2,
        5,
        List.of("MOBGIANTBLACKWIDOW"),
        List.of(
            new SpawnGroup.SpawnPoint(1847, 37, 2),
            new SpawnGroup.SpawnPoint(1849, 54, 2),
            new SpawnGroup.SpawnPoint(1742, 88, 2),
            new SpawnGroup.SpawnPoint(1767, 88, 2),
            new SpawnGroup.SpawnPoint(1759, 102, 2),
            new SpawnGroup.SpawnPoint(1731, 103, 2),
            new SpawnGroup.SpawnPoint(1757, 110, 2),
            new SpawnGroup.SpawnPoint(1730, 120, 2),
            new SpawnGroup.SpawnPoint(1754, 126, 2),
            new SpawnGroup.SpawnPoint(1752, 141, 2),
            new SpawnGroup.SpawnPoint(1873, 178, 2),
            new SpawnGroup.SpawnPoint(2073, 185, 2),
            new SpawnGroup.SpawnPoint(2060, 196, 2),
            new SpawnGroup.SpawnPoint(2072, 201, 2),
            new SpawnGroup.SpawnPoint(1726, 221, 2),
            new SpawnGroup.SpawnPoint(1753, 232, 2),
            new SpawnGroup.SpawnPoint(1772, 232, 2),
            new SpawnGroup.SpawnPoint(1727, 241, 2),
            new SpawnGroup.SpawnPoint(1774, 248, 2),
            new SpawnGroup.SpawnPoint(1725, 253, 2),
            new SpawnGroup.SpawnPoint(1764, 262, 2),
            new SpawnGroup.SpawnPoint(1747, 263, 2),
            new SpawnGroup.SpawnPoint(1745, 280, 2),
            new SpawnGroup.SpawnPoint(1745, 281, 2),
            new SpawnGroup.SpawnPoint(1905, 311, 2),
            new SpawnGroup.SpawnPoint(1896, 318, 2),
            new SpawnGroup.SpawnPoint(1916, 322, 2),
            new SpawnGroup.SpawnPoint(1907, 329, 2)));
  }
}
