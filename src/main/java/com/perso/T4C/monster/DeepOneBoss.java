package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;

public final class DeepOneBoss extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public DeepOneBoss(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "DEEPONEBOSS",
        "${monster.deeponeboss}",
        1812,
        0,
        25,
        23307,
        45,
        102,
        30000L,
        "AtrocityBoss#h",
        "AtrocityBossA#h",
        "AtrocityBossC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        150,
        462,
        java.util.List.of(),
        false,
        0.0f,
        57,
        52,
        52,
        65,
        52,
        52,
        23,
        new int[] {74, 74, 49, 98, 74, 5000, 100, 100, 100, 100, 100, 100},
        42,
        178,
        0,
        1101529088,
        20040,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d58+44)", 514, 50, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
