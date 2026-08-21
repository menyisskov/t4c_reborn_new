package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Corrupt Follower", x = 819, y = 1506, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 819, y = 1546, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 821, y = 1515, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 840, y = 1785, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 847, y = 1748, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 847, y = 1793, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 858, y = 1522, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 859, y = 1629, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 864, y = 1643, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 865, y = 1607, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 869, y = 1729, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 872, y = 1785, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 880, y = 1654, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 881, y = 1571, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 902, y = 1793, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 905, y = 1755, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 909, y = 1795, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 910, y = 1790, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 911, y = 1550, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Corrupt Follower", x = 936, y = 1558, z = 1, stationary = false, aggressive = true)
public final class CorruptFollower extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Corrupt Follower";

  public CorruptFollower(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Corrupt Follower",
        "${monster.corrupt_follower}",
        463,
        0,
        3,
        810,
        22,
        50,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        44,
        137,
        java.util.List.of(),
        false,
        0.0f,
        40,
        37,
        37,
        45,
        0,
        37,
        0,
        new int[] {54, 109, 82, 82, 82, 5000, 100, 100, 100, 100, 100, 100},
        25,
        110,
        0,
        1076363264,
        20034,
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
        java.util.List.of(new MonsterDef.Attack("1d29+21", 310, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
