package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class GABRIELARCHONIS extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public GABRIELARCHONIS(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "GABRIELARCHONIS",
        "${monster.gabrielarchonis}",
        10000000,
        0,
        0,
        0,
        1,
        5,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        165,
        149,
        149,
        195,
        149,
        149,
        45,
        new int[] {5000, 5000, 5000, 5000, 5000, 5000, 100, 100, 100, 100, 100, 100},
        150,
        0,
        0,
        1176255488,
        10011,
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
        java.util.List.of(
            new MonsterDef.Attack(
                "if(target.hp>10?if(target.hp<200?target.hp-10:190-1d5):0)", 1810, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
