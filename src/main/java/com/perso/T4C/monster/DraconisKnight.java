package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DraconisKnight extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Draconis Knight";

  public DraconisKnight(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Draconis Knight",
        "${monster.draconis_knight}",
        1961,
        0,
        7,
        7198,
        88,
        201,
        30000L,
        "MonsDraconianPlate#k",
        "MonsDraconianPlateA#k",
        "MonsDraconianPlateC#l",
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
        20065,
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
        java.util.List.of(new MonsterDef.Attack("1d114+87", 874, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
