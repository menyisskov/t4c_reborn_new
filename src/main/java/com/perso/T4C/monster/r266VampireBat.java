package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Vampire Bat", x = 1029, y = 1606, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 1047, y = 1586, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 1055, y = 1575, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 1061, y = 1587, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 612, y = 1608, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 618, y = 1634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 629, y = 1599, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 643, y = 1580, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 647, y = 1630, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 718, y = 1807, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 724, y = 1791, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 743, y = 1814, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 759, y = 1848, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 762, y = 1753, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 766, y = 1455, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 784, y = 1852, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 789, y = 1604, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 791, y = 1554, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 791, y = 1751, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 797, y = 1841, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 803, y = 1421, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 809, y = 1644, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 816, y = 1575, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 816, y = 1779, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 819, y = 1535, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 821, y = 1457, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 825, y = 1439, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 829, y = 1650, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 831, y = 1779, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 832, y = 1473, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 832, y = 1682, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 834, y = 1435, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 840, y = 1544, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 842, y = 1668, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 843, y = 1661, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 846, y = 1482, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 848, y = 1490, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 856, y = 1568, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 858, y = 1492, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 866, y = 1573, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 930, y = 1701, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 945, y = 1711, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 951, y = 1665, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 964, y = 1546, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 980, y = 1553, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Vampire Bat", x = 995, y = 1583, z = 2, stationary = false, aggressive = true)
public final class r266VampireBat extends DataMonster {
  public static final String SOUND_ATTACK = "Bat Attack.wav";
  public static final String SOUND_DEATH = "Bat Dying.wav";
  public static final String SOUND_HIT = "Bat Hit.wav";

  public r266VampireBat(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Vampire Bat",
        "${monster.vampire_bat}",
        792,
        0,
        4,
        1874,
        38,
        87,
        30000L,
        "Bat#h",
        "BatA#i",
        "BatC#l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        68,
        209,
        java.util.List.of(new MonsterDef.LootDrop("Vampire bat wings", 0.04f)),
        false,
        0.0f,
        53,
        49,
        49,
        60,
        0,
        49,
        0,
        new int[] {50, 101, 101, 50, 5025, 50, 100, 100, 100, 100, 100, 100},
        38,
        257,
        0,
        0,
        20002,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d50+37", 466, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 75, 10119, 3, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
