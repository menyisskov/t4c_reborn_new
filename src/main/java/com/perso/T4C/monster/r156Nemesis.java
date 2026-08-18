package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Nemesis", x = 1665, y = 200, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1667, y = 181, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1684, y = 164, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1684, y = 179, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1685, y = 216, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1690, y = 200, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1696, y = 181, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1702, y = 172, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1702, y = 202, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1702, y = 213, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1714, y = 179, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1719, y = 198, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1866, y = 311, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1878, y = 291, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1879, y = 321, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1900, y = 231, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1903, y = 292, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1907, y = 246, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1913, y = 209, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1913, y = 351, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1916, y = 234, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1920, y = 215, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1928, y = 289, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1929, y = 323, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1933, y = 249, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1939, y = 205, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1946, y = 264, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 1953, y = 270, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 2004, y = 268, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 2007, y = 313, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 2012, y = 331, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 2021, y = 228, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 2021, y = 327, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 2022, y = 302, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 2022, y = 309, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 2023, y = 235, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 2023, y = 274, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 2032, y = 241, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nemesis", x = 2045, y = 265, z = 2, stationary = false, aggressive = true)
public final class r156Nemesis extends DataMonster {
  public static final String SOUND_ATTACK = "Elemear Attack.wav";
  public static final String SOUND_DEATH = "Scorpion Dying.wav";
  public static final String SOUND_HIT = "Scorpion Hit.wav";

  public r156Nemesis(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Nemesis",
        "${monster.nemesis}",
        2301,
        0,
        8,
        9409,
        108,
        245,
        30000L,
        "Scorpion#h",
        "ScorpionA#g",
        "ScorpionC#l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        143,
        440,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough moonstone", 0.001f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough agate", 0.02f)),
        false,
        0.0f,
        95,
        86,
        86,
        111,
        0,
        86,
        0,
        new int[] {67, 67, 45, 90, 67, 5000, 100, 100, 100, 100, 100, 100},
        80,
        330,
        0,
        1078198272,
        20024,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        40,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d138+107", 970, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10317, 3, 10),
            new MonsterDef.Attack("", 0, 10, 10321, 3, 10),
            new MonsterDef.Attack("", 0, 70, 10091, 3, 10),
            new MonsterDef.Attack("", 0, 10, 10346, 3, 10),
            new MonsterDef.Attack("", 0, 3, 10344, 0, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
