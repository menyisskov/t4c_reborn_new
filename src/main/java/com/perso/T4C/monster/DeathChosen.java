package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Death Chosen", x = 654, y = 1558, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 668, y = 1535, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 683, y = 1510, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 693, y = 1562, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 757, y = 1493, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 766, y = 1481, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 783, y = 1730, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 833, y = 1432, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 839, y = 1699, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 846, y = 1412, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 857, y = 1684, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 864, y = 1656, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 868, y = 1703, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 873, y = 1663, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 878, y = 1651, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 880, y = 1643, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 883, y = 1630, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 883, y = 1749, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 884, y = 1731, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 890, y = 1757, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 894, y = 1614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 895, y = 1614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 895, y = 1733, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 898, y = 1650, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 901, y = 1773, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 914, y = 1710, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 915, y = 1719, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 915, y = 1773, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 918, y = 1609, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 921, y = 1613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 921, y = 1614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 923, y = 1725, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 927, y = 1648, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 936, y = 1679, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 936, y = 1711, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 939, y = 1696, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 950, y = 1673, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 952, y = 1614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 953, y = 1691, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 961, y = 1623, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 963, y = 1614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 973, y = 1606, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Death Chosen", x = 977, y = 1588, z = 2, stationary = false, aggressive = true)
public final class DeathChosen extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public static final String CANONICAL_NAME = "Death Chosen";

  public DeathChosen(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Death Chosen",
        "${monster.death_chosen}",
        847,
        0,
        4,
        2090,
        42,
        94,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        71,
        220,
        java.util.List.of(
            new MonsterDef.LootDrop("Gleaming shard", 0.02f),
            new MonsterDef.LootDrop("Potion of fury", 0.05f),
            new MonsterDef.LootDrop("Red cape", 0.01f),
            new MonsterDef.LootDrop("Defender", 0.01f),
            new MonsterDef.LootDrop("Stone key", 0.01f),
            new MonsterDef.LootDrop("Skeleton bone", 0.001f),
            new MonsterDef.LootDrop("Scalemail leggings", 2.0E-4f),
            new MonsterDef.LootDrop("Scalemail gauntlets", 2.0E-4f)),
        false,
        0.0f,
        55,
        50,
        50,
        63,
        0,
        50,
        0,
        new int[] {75, 75, 100, 50, 5025, 50, 100, 100, 100, 100, 100, 100},
        40,
        170,
        0,
        1077149696,
        20012,
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
            new MonsterDef.Attack("1d53+41", 490, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 80, 10119, 4, 20)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
