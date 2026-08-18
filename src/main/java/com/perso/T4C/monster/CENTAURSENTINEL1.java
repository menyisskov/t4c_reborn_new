package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class CENTAURSENTINEL1 extends DataMonster {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public static final String CANONICAL_NAME = "CENTAURSENTINEL1";

  public CENTAURSENTINEL1(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "CENTAURSENTINEL1",
        "${monster.centaursentinel1}",
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
