package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class CENTAURTRACKER1 extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Taunting Hit.wav";
  public static final String SOUND_HIT = "Centaur Hit.wav";

  public static final String CANONICAL_NAME = "CENTAURTRACKER1";

  public CENTAURTRACKER1(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "CENTAURTRACKER1",
        "${monster.centaurtracker1}",
        847,
        0,
        24,
        10452,
        42,
        94,
        30000L,
        "64kCentaurWarrior#i",
        "64kCentaurWarriorA#i",
        "64kCentaurWarriorC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
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
