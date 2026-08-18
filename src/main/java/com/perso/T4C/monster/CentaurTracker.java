package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "CENTAURTRACKER", x = 1034, y = 1184, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 1078, y = 1042, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 1114, y = 1116, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 1139, y = 1230, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 1145, y = 1050, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 1163, y = 1261, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 1182, y = 1162, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 1197, y = 1043, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 766, y = 970, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 770, y = 949, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 787, y = 925, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 790, y = 1004, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 809, y = 1029, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 823, y = 964, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 834, y = 1018, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 845, y = 941, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 845, y = 978, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 846, y = 920, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 864, y = 940, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 892, y = 922, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 913, y = 904, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 927, y = 831, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 931, y = 881, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 936, y = 870, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 951, y = 1171, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 952, y = 939, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 965, y = 1105, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 966, y = 826, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 970, y = 995, z = 0, stationary = false, aggressive = false)
@Spawn(type = "CENTAURTRACKER", x = 985, y = 913, z = 0, stationary = false, aggressive = false)
public final class CentaurTracker extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Centaur Dying.wav";
  public static final String SOUND_HIT = "Centaur Hit.wav";

  public CentaurTracker(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "CENTAURTRACKER",
        "${monster.centaurtracker}",
        847,
        0,
        24,
        10452,
        42,
        94,
        30000L,
        "64kCentaurWarrior#i",
        "64kCentaurWarriorA#i",
        "64kCentaurWarriorC#n",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        71,
        220,
        java.util.List.of(),
        false,
        0.0f,
        55,
        50,
        50,
        63,
        50,
        50,
        23,
        new int[] {150, 150, 150, 150, 100, 5001, 100, 100, 100, 100, 100, 100},
        40,
        170,
        0,
        1101004800,
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
        java.util.List.of(new MonsterDef.Attack("1d53+41", 490, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
