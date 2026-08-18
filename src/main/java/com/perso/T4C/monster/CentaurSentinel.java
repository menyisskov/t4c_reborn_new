package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;

public final class CentaurSentinel extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Centaur Dying.wav";
  public static final String SOUND_HIT = "Centaur Hit.wav";

  public CentaurSentinel(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "CENTAURSENTINEL",
        "${monster.centaursentinel}",
        1321,
        0,
        30,
        20312,
        63,
        143,
        30000L,
        "64kCentaurWarrior#i",
        "64kCentaurWarriorA#i",
        "64kCentaurWarriorC#n",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        98,
        302,
        java.util.List.of(),
        false,
        0.0f,
        70,
        64,
        64,
        81,
        64,
        64,
        26,
        new int[] {136, 136, 136, 136, 90, 5001, 100, 100, 100, 100, 100, 100},
        55,
        230,
        0,
        1104674816,
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
        java.util.List.of(new MonsterDef.Attack("1d81+62", 670, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
