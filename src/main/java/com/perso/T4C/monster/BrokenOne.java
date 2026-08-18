package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Broken One", x = 648, y = 1716, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 651, y = 1573, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 663, y = 1561, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 664, y = 1687, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 674, y = 1629, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 683, y = 1539, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 691, y = 1614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 705, y = 1751, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 706, y = 1579, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 706, y = 1657, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 710, y = 1737, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 722, y = 1614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 723, y = 1704, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 730, y = 1636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 743, y = 1584, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 744, y = 1713, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 895, y = 1436, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 917, y = 1411, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 923, y = 1482, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 938, y = 1481, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 948, y = 1447, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 954, y = 1467, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Broken One", x = 957, y = 1490, z = 2, stationary = false, aggressive = true)
public final class BrokenOne extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public static final String CANONICAL_NAME = "Broken One";

  public BrokenOne(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Broken One",
        "${monster.broken_one}",
        876,
        0,
        5,
        2203,
        43,
        98,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC#j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        73,
        225,
        java.util.List.of(new MonsterDef.LootDrop("Gleaming shard", 0.02f)),
        false,
        0.0f,
        56,
        51,
        51,
        64,
        0,
        51,
        0,
        new int[] {74, 74, 99, 49, 5025, 49, 100, 100, 100, 100, 100, 100},
        41,
        174,
        0,
        1077149696,
        20009,
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
            new MonsterDef.Attack("1d56+42", 502, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 30, 10119, 7, 15),
            new MonsterDef.Attack("", 0, 45, 10086, 7, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
