package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "GAENENELTHORN", x = 2660, y = 2416, z = 2, stationary = false, aggressive = false)
public final class GAENENELTHORN extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public GAENENELTHORN(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "GAENENELTHORN",
        "${monster.gaenenelthorn}",
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
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
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
