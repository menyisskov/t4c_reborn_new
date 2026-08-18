package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0255 {
  private SpawnGroup0255() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Corrupt Follower",
        30,
        60,
        3,
        5,
        List.of("Corrupt Follower"),
        List.of(
            new SpawnGroup.SpawnPoint(819, 1506, 1),
            new SpawnGroup.SpawnPoint(821, 1515, 1),
            new SpawnGroup.SpawnPoint(858, 1522, 1),
            new SpawnGroup.SpawnPoint(819, 1546, 1),
            new SpawnGroup.SpawnPoint(911, 1550, 1),
            new SpawnGroup.SpawnPoint(936, 1558, 1),
            new SpawnGroup.SpawnPoint(881, 1571, 1),
            new SpawnGroup.SpawnPoint(865, 1607, 1),
            new SpawnGroup.SpawnPoint(859, 1629, 1),
            new SpawnGroup.SpawnPoint(864, 1643, 1),
            new SpawnGroup.SpawnPoint(880, 1654, 1),
            new SpawnGroup.SpawnPoint(869, 1729, 1),
            new SpawnGroup.SpawnPoint(847, 1748, 1),
            new SpawnGroup.SpawnPoint(905, 1755, 1),
            new SpawnGroup.SpawnPoint(840, 1785, 1),
            new SpawnGroup.SpawnPoint(872, 1785, 1),
            new SpawnGroup.SpawnPoint(910, 1790, 1),
            new SpawnGroup.SpawnPoint(847, 1793, 1),
            new SpawnGroup.SpawnPoint(902, 1793, 1),
            new SpawnGroup.SpawnPoint(909, 1795, 1)));
  }
}
