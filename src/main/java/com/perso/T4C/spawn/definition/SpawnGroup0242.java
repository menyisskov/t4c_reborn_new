package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0242 {
  private SpawnGroup0242() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Worshipper",
        30,
        60,
        3,
        5,
        List.of("Worshipper"),
        List.of(
            new SpawnGroup.SpawnPoint(1485, 1503, 1),
            new SpawnGroup.SpawnPoint(1466, 1507, 1),
            new SpawnGroup.SpawnPoint(1490, 1516, 1),
            new SpawnGroup.SpawnPoint(1462, 1522, 1),
            new SpawnGroup.SpawnPoint(1480, 1528, 1),
            new SpawnGroup.SpawnPoint(1514, 1554, 1),
            new SpawnGroup.SpawnPoint(1522, 1562, 1),
            new SpawnGroup.SpawnPoint(1700, 1740, 1),
            new SpawnGroup.SpawnPoint(1529, 1743, 1),
            new SpawnGroup.SpawnPoint(1710, 1748, 1),
            new SpawnGroup.SpawnPoint(1522, 1751, 1),
            new SpawnGroup.SpawnPoint(1469, 1774, 1),
            new SpawnGroup.SpawnPoint(1480, 1780, 1),
            new SpawnGroup.SpawnPoint(1746, 1780, 1),
            new SpawnGroup.SpawnPoint(1763, 1780, 1),
            new SpawnGroup.SpawnPoint(1742, 1792, 1),
            new SpawnGroup.SpawnPoint(1774, 1792, 1),
            new SpawnGroup.SpawnPoint(1466, 1795, 1),
            new SpawnGroup.SpawnPoint(1491, 1801, 1),
            new SpawnGroup.SpawnPoint(1478, 1805, 1),
            new SpawnGroup.SpawnPoint(1748, 1805, 1)));
  }
}
