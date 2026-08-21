package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "Olin Haad Guard 3",
    x = 2811,
    y = 1107,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2830,
    y = 1170,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2831,
    y = 1091,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2832,
    y = 1133,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2847,
    y = 1106,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2860,
    y = 1152,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2861,
    y = 1124,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2862,
    y = 1173,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2867,
    y = 1086,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2881,
    y = 1140,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2883,
    y = 1072,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2893,
    y = 1176,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2895,
    y = 1158,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2896,
    y = 1091,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2904,
    y = 1054,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2916,
    y = 1110,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2917,
    y = 1052,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2925,
    y = 1066,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2931,
    y = 1108,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2935,
    y = 1041,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2939,
    y = 1080,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2944,
    y = 1096,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2964,
    y = 1068,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 2980,
    y = 1092,
    z = 0,
    stationary = false,
    aggressive = false)
@Spawn(
    type = "Olin Haad Guard 3",
    x = 3015,
    y = 1013,
    z = 0,
    stationary = false,
    aggressive = false)
public final class r163OlinHaadGuard3 extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public r163OlinHaadGuard3(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Guard 3",
        "${monster.olin_haad_guard_3}",
        55,
        0,
        1,
        46,
        4,
        8,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC!a",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        5,
        16,
        java.util.List.of(),
        false,
        0.0f,
        18,
        17,
        17,
        18,
        0,
        17,
        0,
        new int[] {92, 92, 92, 92, 61, 5000, 100, 100, 100, 100, 100, 100},
        3,
        22,
        0,
        1072693248,
        20006,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        1,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d5+3", 46, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
