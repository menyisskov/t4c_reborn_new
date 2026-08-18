package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class MercenaryC extends DataMonster {

  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public MercenaryC(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBMERCENARYC",
        "${monster.mobmercenaryc}",
        116,
        0,
        9,
        563,
        6,
        15,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC#l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        12,
        38,
        java.util.List.of(),
        false,
        0.0f,
        21,
        21,
        21,
        23,
        21,
        21,
        21,
        new int[] {90, 90, 90, 90, 60, 5000, 100, 100, 100, 100, 100, 100},
        7,
        43,
        0,
        1073741824,
        10004,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d10+5", 94, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
