package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DRAGON extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public DRAGON(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Dragon",
        "${monster.dragon}",
        1000000,
        0,
        0,
        0,
        1,
        4,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        20,
        22,
        24,
        20,
        10000,
        21,
        22,
        new int[] {5000, 5000, 5000, 5000, 5000, 5000, 100, 100, 100, 100, 100, 100},
        100,
        65535,
        0,
        1232348160,
        20028,
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
