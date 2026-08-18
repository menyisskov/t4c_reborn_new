package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class CorruptAdept extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Corrupt Adept";

  public CorruptAdept(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Corrupt Adept",
        "${monster.corrupt_adept}",
        418,
        0,
        3,
        699,
        20,
        46,
        30000L,
        "Kraanian#h",
        "KraanianA#h",
        "KraanianC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        41,
        126,
        java.util.List.of(),
        false,
        0.0f,
        38,
        35,
        35,
        42,
        0,
        35,
        0,
        new int[] {110, 55, 83, 83, 83, 5000, 100, 100, 100, 100, 100, 100},
        23,
        102,
        0,
        1076232192,
        20025,
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
        java.util.List.of(new MonsterDef.Attack("1d27+19", 286, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
