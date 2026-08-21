package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "CENTAURSENTINEL", x = 1000, y = 1119, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 1010, y = 1026, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 1014, y = 1095, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 1116, y = 1147, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 1148, y = 1080, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 1719, y = 531, z = 2, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 1732, y = 525, z = 2, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 1742, y = 509, z = 2, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 1754, y = 519, z = 2, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 1779, y = 542, z = 2, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 1784, y = 550, z = 2, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 1784, y = 587, z = 2, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 1795, y = 577, z = 2, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 767, y = 996, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 774, y = 936, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 816, y = 1018, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 838, y = 922, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 852, y = 951, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 873, y = 1151, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 892, y = 961, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 897, y = 997, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 923, y = 1020, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 927, y = 890, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 931, y = 1110, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 933, y = 900, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 937, y = 974, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 957, y = 1019, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 975, y = 918, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 987, y = 1063, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURSENTINEL", x = 999, y = 1016, z = 0, stationary = false, aggressive = false)
public final class CentaurSentinel extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Taunting Hit.wav";
  public static final String SOUND_HIT = "Centaur Hit.wav";

  public CentaurSentinel(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "CENTAURSENTINEL",
        "${monster.centaursentinel}",
        1321,
        0,
        30,
        20312,
        63,
        143,
        30000L,
        "64kCentaurWarrior#i",
        "64kCentaurWarriorA#i",
        "64kCentaurWarriorC!n",
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
        64,
        64,
        26,
        new int[] {136, 136, 136, 136, 90, 5001, 100, 100, 100, 100, 100, 100},
        55,
        230,
        0,
        1104674816,
        20051,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d81+62", 670, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
