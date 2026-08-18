package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Antelope extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Antelope";

  public Antelope(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Antelope",
        "${monster.antelope}",
        353,
        0,
        0,
        0,
        0,
        0,
        30000L,
        "Ori#h",
        "OriA",
        "OriC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(new MonsterDef.LootDrop("Antelope skin", 0.2f)),
        false,
        0.0f,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        new int[] {56, 56, 56, 56, 56, 5000, 100, 100, 100, 100, 100, 100},
        20,
        90,
        0,
        1076101120,
        20032,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        13,
        0,
        false,
        java.util.List.of(),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
