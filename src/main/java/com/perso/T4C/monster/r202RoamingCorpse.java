package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r202RoamingCorpse extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public r202RoamingCorpse(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Roaming Corpse",
        "${monster.roaming_corpse}",
        996,
        0,
        5,
        2708,
        50,
        113,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC#j",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        80,
        247,
        java.util.List.of(),
        false,
        0.0f,
        60,
        55,
        55,
        69,
        0,
        55,
        0,
        new int[] {72, 72, 96, 48, 5025, 48, 100, 100, 100, 100, 100, 100},
        45,
        190,
        0,
        1077280768,
        20009,
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
        java.util.List.of(new MonsterDef.Attack("1d64+49", 550, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
