package com.perso.T4C.spawn.definition;

import com.perso.T4C.monster.SpawnGroup;
import com.perso.T4C.spawn.SpawnGroupDefinition;
import java.util.List;

public final class SpawnGroup0047 {
  private SpawnGroup0047() {}

  public static SpawnGroupDefinition definition() {
    return new SpawnGroupDefinition(
        "Crypt Custodian",
        120,
        240,
        1,
        5,
        List.of("Crypt Custodian"),
        List.of(
            new SpawnGroup.SpawnPoint(1625, 602, 2),
            new SpawnGroup.SpawnPoint(1626, 610, 2),
            new SpawnGroup.SpawnPoint(1615, 619, 2),
            new SpawnGroup.SpawnPoint(1607, 623, 2),
            new SpawnGroup.SpawnPoint(1604, 634, 2),
            new SpawnGroup.SpawnPoint(1595, 637, 2),
            new SpawnGroup.SpawnPoint(1584, 648, 2),
            new SpawnGroup.SpawnPoint(1574, 655, 2),
            new SpawnGroup.SpawnPoint(1583, 663, 2),
            new SpawnGroup.SpawnPoint(1564, 675, 2),
            new SpawnGroup.SpawnPoint(1594, 675, 2),
            new SpawnGroup.SpawnPoint(1549, 682, 2),
            new SpawnGroup.SpawnPoint(1575, 696, 2),
            new SpawnGroup.SpawnPoint(1569, 699, 2),
            new SpawnGroup.SpawnPoint(1566, 702, 2),
            new SpawnGroup.SpawnPoint(1600, 702, 2),
            new SpawnGroup.SpawnPoint(1581, 715, 2)));
  }
}
