package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBMERCENARYA", x = 2664, y = 1072, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYA", x = 2669, y = 1082, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYA", x = 2672, y = 1068, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYA", x = 2677, y = 1072, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYA", x = 2678, y = 1059, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYA", x = 2682, y = 1066, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYA", x = 2688, y = 1066, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYA", x = 2694, y = 1062, z = 0, stationary = false, aggressive = true)
public class MercenaryA extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public MercenaryA(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBMERCENARYA",
        "${monster.mobmercenarya}",
        116,
        0,
        9,
        563,
        6,
        15,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        12,
        38,
        java.util.List.of(),
        false,
        0.0f,
        21,
        21,
        21,
        23,
        21,
        21,
        21,
        new int[] {90, 90, 90, 90, 60, 5000, 100, 100, 100, 100, 100, 100},
        7,
        43,
        0,
        1073741824,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d10+5", 94, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
