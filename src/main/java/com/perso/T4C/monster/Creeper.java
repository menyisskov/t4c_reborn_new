package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Creeper", x = 1486, y = 1651, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1487, y = 1660, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1507, y = 1677, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1529, y = 1690, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1530, y = 1621, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1542, y = 1726, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1546, y = 1590, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1548, y = 1671, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1556, y = 1632, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1574, y = 1694, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1576, y = 1566, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1580, y = 1741, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1580, y = 1752, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1581, y = 1554, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1597, y = 1593, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1610, y = 1532, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1615, y = 1526, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1618, y = 1582, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1627, y = 1722, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1632, y = 1770, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1637, y = 1727, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1638, y = 1547, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1652, y = 1749, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1654, y = 1696, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1659, y = 1607, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1660, y = 1688, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1666, y = 1733, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1669, y = 1518, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1671, y = 1522, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1677, y = 1596, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1690, y = 1711, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1691, y = 1486, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1691, y = 1492, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1701, y = 1694, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1702, y = 1569, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1715, y = 1557, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1718, y = 1552, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1721, y = 1634, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1725, y = 1547, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1731, y = 1665, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1780, y = 1583, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Creeper", x = 1813, y = 1543, z = 1, stationary = false, aggressive = true)
public final class Creeper extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Creeper";

  public Creeper(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Creeper",
        "${monster.creeper}",
        1219,
        0,
        5,
        3615,
        59,
        133,
        30000L,
        "Atrocity#h",
        "AtrocityA#h",
        "AtrocityC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        93,
        286,
        java.util.List.of(),
        false,
        0.0f,
        67,
        61,
        61,
        77,
        0,
        61,
        0,
        new int[] {69, 69, 46, 92, 69, 5000, 100, 100, 100, 100, 100, 100},
        52,
        218,
        0,
        1077542912,
        20026,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d75+58", 634, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
