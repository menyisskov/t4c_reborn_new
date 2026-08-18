package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class CENTAURAVENGER1 extends DataMonster {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public static final String CANONICAL_NAME = "CENTAURAVENGER1";

  public CENTAURAVENGER1(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "CENTAURAVENGER1",
        "${monster.centauravenger1}",
        1497,
        0,
        32,
        24348,
        70,
        159,
        30000L,
        "64kCentaurKing#i",
        "64kCentaurKingA#i",
        "64kCentaurKingC#n",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        107,
        330,
        java.util.List.of(),
        false,
        0.0f,
        75,
        68,
        68,
        87,
        68,
        68,
        27,
        new int[] {130, 130, 130, 130, 86, 5001, 100, 100, 100, 100, 100, 100},
        60,
        250,
        0,
        1106247680,
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
        java.util.List.of(new MonsterDef.Attack("1d90+69", 730, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
