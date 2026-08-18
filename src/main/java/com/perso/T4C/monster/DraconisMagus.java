package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DraconisMagus extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Draconis Magus";

  public DraconisMagus(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Draconis Magus",
        "${monster.draconis_magus}",
        1961,
        0,
        7,
        7198,
        88,
        201,
        30000L,
        "MonsDraconianRobe#k",
        "MonsDraconianRobeA#k",
        "MonsDraconianRobeC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        129,
        396,
        java.util.List.of(),
        false,
        0.0f,
        87,
        79,
        79,
        101,
        0,
        79,
        0,
        new int[] {80, 80, 80, 80, 80, 5000, 100, 100, 100, 100, 100, 100},
        72,
        298,
        0,
        1078067200,
        20066,
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
        java.util.List.of(
            new MonsterDef.Attack("1d114+87", 874, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 40, 10095, 1, 15),
            new MonsterDef.Attack("", 0, 4, 10449, 1, 15),
            new MonsterDef.Attack("", 0, 30, 10086, 1, 15),
            new MonsterDef.Attack("", 0, 26, 10120, 1, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
