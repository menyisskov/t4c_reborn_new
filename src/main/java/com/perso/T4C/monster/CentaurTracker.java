package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;

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
