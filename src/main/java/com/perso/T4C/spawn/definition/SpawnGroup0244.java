package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0244 {
  private SpawnGroup0244() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Apparition",
        30,
        60,
        3,
        5,
        List.of("Apparition"),
        List.of(
            new SpawnGroup.SpawnPoint(1996, 1935, 1),
            new SpawnGroup.SpawnPoint(1954, 1940, 1),
            new SpawnGroup.SpawnPoint(1992, 1942, 1),
            new SpawnGroup.SpawnPoint(1967, 1943, 1),
            new SpawnGroup.SpawnPoint(1976, 1950, 1),
            new SpawnGroup.SpawnPoint(1951, 1952, 1),
            new SpawnGroup.SpawnPoint(1939, 1958, 1),
            new SpawnGroup.SpawnPoint(1978, 1959, 1),
            new SpawnGroup.SpawnPoint(1968, 1965, 1),
            new SpawnGroup.SpawnPoint(1993, 1965, 1),
            new SpawnGroup.SpawnPoint(1959, 1967, 1),
            new SpawnGroup.SpawnPoint(1949, 1970, 1),
            new SpawnGroup.SpawnPoint(1984, 1976, 1),
            new SpawnGroup.SpawnPoint(1963, 1982, 1),
            new SpawnGroup.SpawnPoint(1953, 1983, 1),
            new SpawnGroup.SpawnPoint(1983, 1988, 1),
            new SpawnGroup.SpawnPoint(1970, 1990, 1),
            new SpawnGroup.SpawnPoint(1947, 1993, 1),
            new SpawnGroup.SpawnPoint(1931, 1998, 1),
            new SpawnGroup.SpawnPoint(1932, 2001, 1),
            new SpawnGroup.SpawnPoint(1920, 2011, 1)));
  }
}
