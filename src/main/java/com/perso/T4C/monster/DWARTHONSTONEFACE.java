package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "DWARTHONSTONEFACE", x = 303, y = 1678, z = 0, stationary = false, aggressive = false)
public final class DWARTHONSTONEFACE extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public DWARTHONSTONEFACE(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "DWARTHONSTONEFACE",
        "${monster.dwarthonstoneface}",
        1992,
        0,
        27,
        27084,
        50,
        113,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        160,
        494,
        java.util.List.of(),
        false,
        0.0f,
        60,
        55,
        55,
        69,
        55,
        55,
        24,
        new int[] {72, 72, 72, 72, 48, 5000, 100, 100, 100, 100, 100, 100},
        45,
        190,
        0,
        1102053376,
        0,
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
        java.util.List.of(new MonsterDef.Attack("1d64+49", 550, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
