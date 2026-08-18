package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Shrieking Horror", x = 491, y = 2482, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 497, y = 2542, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 509, y = 2480, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 511, y = 2556, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 514, y = 2403, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 515, y = 2423, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 515, y = 2494, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 517, y = 2531, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 527, y = 2063, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 527, y = 2563, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 528, y = 2430, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 530, y = 2383, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 530, y = 2412, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 542, y = 2253, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 542, y = 2399, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 543, y = 2058, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 549, y = 2229, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 554, y = 2488, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 558, y = 2082, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 558, y = 2163, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 559, y = 2549, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 560, y = 2097, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 560, y = 2119, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 563, y = 2182, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 563, y = 2227, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 568, y = 2486, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 571, y = 2175, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 572, y = 2209, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 573, y = 2474, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 580, y = 2107, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 581, y = 2537, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 587, y = 2203, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 590, y = 2523, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 594, y = 2029, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 599, y = 2089, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 606, y = 2033, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 611, y = 2076, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 617, y = 2082, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 621, y = 2389, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 627, y = 2368, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 636, y = 2071, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 644, y = 2404, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 655, y = 2381, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 680, y = 2391, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 682, y = 2373, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 684, y = 2395, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Shrieking Horror", x = 689, y = 2378, z = 1, stationary = false, aggressive = true)
public final class r213ShriekingHorror extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Mummy Dying.wav";
  public static final String SOUND_HIT = "Mummy Hit.wav";

  public r213ShriekingHorror(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Shrieking Horror",
        "${monster.shrieking_horror}",
        967,
        0,
        5,
        2574,
        48,
        109,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        79,
        242,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Healing potion", 0.02f),
            new MonsterDef.LootDrop("Sapphire bracelet", 0.01f),
            new MonsterDef.LootDrop("Potion of fury", 0.05f),
            new MonsterDef.LootDrop("Mummy bandages", 0.02f)),
        false,
        0.0f,
        59,
        54,
        54,
        67,
        0,
        54,
        0,
        new int[] {73, 73, 97, 48, 5025, 48, 100, 100, 100, 100, 100, 100},
        44,
        186,
        0,
        1077280768,
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
            new MonsterDef.Attack("1d62+47", 538, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 60, 10120, 10, 14),
            new MonsterDef.Attack("", 0, 10, 10119, 10, 14),
            new MonsterDef.Attack("", 0, 18, 10086, 10, 14),
            new MonsterDef.Attack("", 0, 18, 10096, 2, 9)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
