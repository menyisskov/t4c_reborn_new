package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0039 {
  private SpawnGroup0039() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Neoflare",
        120,
        240,
        1,
        7,
        List.of("MOBNEOFLARE"),
        List.of(
            new SpawnGroup.SpawnPoint(1870, 2707, 0),
            new SpawnGroup.SpawnPoint(1887, 2728, 0),
            new SpawnGroup.SpawnPoint(1851, 2751, 0),
            new SpawnGroup.SpawnPoint(1834, 2759, 0),
            new SpawnGroup.SpawnPoint(1852, 2766, 0),
            new SpawnGroup.SpawnPoint(1827, 2775, 0),
            new SpawnGroup.SpawnPoint(1878, 2777, 0),
            new SpawnGroup.SpawnPoint(1861, 2782, 0),
            new SpawnGroup.SpawnPoint(1874, 2789, 0),
            new SpawnGroup.SpawnPoint(1857, 2805, 0),
            new SpawnGroup.SpawnPoint(1853, 2866, 0)));
  }
}
