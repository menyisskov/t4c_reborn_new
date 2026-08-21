package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Foul Muck", x = 1513, y = 631, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1520, y = 625, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1526, y = 644, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1533, y = 630, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1533, y = 642, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1620, y = 611, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1629, y = 582, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1632, y = 603, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1647, y = 657, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1651, y = 599, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1651, y = 678, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1652, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1654, y = 568, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1663, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1665, y = 564, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1668, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1674, y = 631, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1680, y = 690, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1681, y = 553, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1688, y = 611, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1691, y = 645, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1695, y = 600, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1697, y = 531, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1701, y = 631, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1707, y = 673, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1719, y = 661, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1720, y = 554, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1720, y = 588, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1723, y = 685, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1734, y = 563, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1741, y = 661, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1742, y = 517, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1742, y = 642, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1745, y = 626, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1771, y = 538, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1772, y = 602, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Foul Muck", x = 1787, y = 592, z = 2, stationary = false, aggressive = true)
public final class FoulMuck extends DataMonster {
  public static final String SOUND_ATTACK = "Ooze Attack.wav";
  public static final String SOUND_DEATH = "Ooze Dying.wav";
  public static final String SOUND_HIT = "Ooze Hit.wav";

  public static final String CANONICAL_NAME = "Foul Muck";

  public FoulMuck(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Foul Muck",
        "${monster.foul_muck}",
        1321,
        0,
        6,
        4062,
        63,
        143,
        30000L,
        "Slime@023",
        "Slimea@35",
        "SlimeC!z",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        98,
        302,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f)),
        false,
        0.0f,
        70,
        64,
        64,
        81,
        0,
        64,
        0,
        new int[] {54, 108, 108, 54, 81, 5000, 100, 100, 100, 100, 100, 100},
        55,
        230,
        0,
        1077608448,
        20005,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        43,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d81+62", 670, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 30, 10348, 6, 10),
            new MonsterDef.Attack("", 0, 3, 10391, 6, 10),
            new MonsterDef.Attack("", 0, 25, 10091, 2, 10),
            new MonsterDef.Attack("", 0, 5, 10333, 2, 5),
            new MonsterDef.Attack("", 0, 10, 10371, 2, 5)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
