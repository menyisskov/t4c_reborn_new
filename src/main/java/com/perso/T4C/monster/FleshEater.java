package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Flesh Eater", x = 100, y = 1694, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 1018, y = 1744, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 1028, y = 1806, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 1044, y = 1751, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 337, y = 1805, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 338, y = 1936, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 342, y = 1898, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 352, y = 1912, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 357, y = 1832, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 364, y = 1803, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 366, y = 1933, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 370, y = 1816, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 393, y = 1920, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 395, y = 1966, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 402, y = 1891, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 421, y = 1861, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 430, y = 1913, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 448, y = 1914, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 454, y = 1767, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 478, y = 1757, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 57, y = 1696, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 59, y = 1684, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 63, y = 1704, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 74, y = 1699, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 77, y = 1678, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 82, y = 1711, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 866, y = 1752, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 88, y = 1674, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 898, y = 1627, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 898, y = 1718, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 905, y = 1819, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 907, y = 1814, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 91, y = 1694, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 917, y = 1849, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 926, y = 1803, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 930, y = 1661, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 939, y = 1847, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 967, y = 1808, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Flesh Eater", x = 971, y = 1758, z = 0, stationary = false, aggressive = true)
public final class FleshEater extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Mummy Dying.wav";
  public static final String SOUND_HIT = "Mummy Hit.wav";

  public static final String CANONICAL_NAME = "Flesh Eater";

  public FleshEater(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Flesh Eater",
        "${monster.flesh_eater}",
        558,
        0,
        3,
        1072,
        26,
        60,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        52,
        159,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Healing potion", 0.03f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Mummy bandages", 0.02f)),
        false,
        0.0f,
        44,
        41,
        41,
        49,
        0,
        41,
        0,
        new int[] {80, 80, 106, 53, 5025, 53, 100, 100, 100, 100, 100, 100},
        29,
        126,
        0,
        1076625408,
        20011,
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
            new MonsterDef.Attack("1d35+25", 358, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 80, 10119, 3, 13)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
