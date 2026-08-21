package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "Olin Haad Soldier 10",
    x = 2965,
    y = 241,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Olin Haad Soldier 10",
    x = 2967,
    y = 250,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Olin Haad Soldier 10",
    x = 2973,
    y = 239,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Olin Haad Soldier 10",
    x = 2976,
    y = 253,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Olin Haad Soldier 10",
    x = 2980,
    y = 241,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Olin Haad Soldier 10",
    x = 2982,
    y = 248,
    z = 0,
    stationary = false,
    aggressive = true)
public final class r168OlinHaadSoldier10 extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public r168OlinHaadSoldier10(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Soldier 10",
        "${monster.olin_haad_soldier_10}",
        164,
        0,
        2,
        184,
        9,
        20,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC!a",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        17,
        55,
        java.util.List.of(),
        false,
        0.0f,
        25,
        23,
        23,
        27,
        0,
        23,
        0,
        new int[] {89, 89, 89, 89, 59, 5000, 100, 100, 100, 100, 100, 100},
        10,
        50,
        0,
        1075052544,
        20006,
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
        java.util.List.of(new MonsterDef.Attack("1d12+8", 130, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
