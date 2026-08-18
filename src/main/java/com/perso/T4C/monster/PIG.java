package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "PIG", x = 1379, y = 167, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 1380, y = 168, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 1381, y = 164, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 1382, y = 168, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 1383, y = 166, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 1384, y = 170, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 1386, y = 168, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 1576, y = 2440, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 1578, y = 2443, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 1582, y = 2450, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 1582, y = 2453, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 1588, y = 2448, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 196, y = 764, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 199, y = 762, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 199, y = 766, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 2582, y = 819, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 2583, y = 818, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 2583, y = 822, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 2585, y = 816, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 2585, y = 820, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 2585, y = 823, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 2587, y = 818, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 2587, y = 822, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 2847, y = 1205, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 2847, y = 1207, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 2850, y = 1205, z = 0, stationary = false, aggressive = false)
@Spawn(type = "PIG", x = 2850, y = 1207, z = 0, stationary = false, aggressive = false)
public final class PIG extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Pig Dying.wav";
  public static final String SOUND_HIT = "Pig Hit.wav";

  public PIG(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "PIG",
        "${monster.pig}",
        300,
        0,
        0,
        0,
        1,
        4,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        20,
        20,
        25,
        55,
        55,
        55,
        55,
        new int[] {75, 75, 75, 75, 75, 5000, 100, 100, 100, 100, 100, 100},
        10,
        30,
        0,
        1073741824,
        20031,
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
        java.util.List.of(new MonsterDef.Attack("1d4", 50, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
