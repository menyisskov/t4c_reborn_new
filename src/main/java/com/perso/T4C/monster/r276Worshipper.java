package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Worshipper", x = 1462, y = 1522, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1466, y = 1507, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1466, y = 1795, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1469, y = 1774, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1478, y = 1805, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1480, y = 1528, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1480, y = 1780, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1485, y = 1503, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1490, y = 1516, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1491, y = 1801, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1514, y = 1554, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1522, y = 1562, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1522, y = 1751, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1529, y = 1743, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1700, y = 1740, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1710, y = 1748, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1742, y = 1792, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1746, y = 1780, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1748, y = 1805, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1763, y = 1780, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Worshipper", x = 1774, y = 1792, z = 1, stationary = false, aggressive = true)
public final class r276Worshipper extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public r276Worshipper(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Worshipper",
        "${monster.worshipper}",
        1321,
        0,
        6,
        4062,
        63,
        143,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        98,
        302,
        java.util.List.of(),
        false,
        0.0f,
        70,
        64,
        64,
        81,
        0,
        64,
        0,
        new int[] {68, 68, 68, 68, 90, 5000, 100, 100, 100, 100, 100, 100},
        55,
        230,
        0,
        1077608448,
        10011,
        41141,
        40022,
        0,
        0,
        0,
        40908,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d81+62", 670, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10094, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
