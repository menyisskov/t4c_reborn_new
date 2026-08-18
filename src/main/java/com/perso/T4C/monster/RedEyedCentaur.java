package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;

public final class RedEyedCentaur extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Centaur Dying.wav";
  public static final String SOUND_HIT = "Centaur Hit.wav";

  public RedEyedCentaur(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBREDEYEDCENTAUR",
        "${monster.mobredeyedcentaur}",
        1000000,
        0,
        0,
        0,
        63,
        143,
        30000L,
        "64kCentaurKing#i",
        "64kCentaurKingA#i",
        "64kCentaurKingC#n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
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
        new int[] {5000, 5000, 5000, 5000, 5000, 5055, 100, 100, 100, 100, 100, 100},
        55,
        65000,
        0,
        1199433472,
        20054,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
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
