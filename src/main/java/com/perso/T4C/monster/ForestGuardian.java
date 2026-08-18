package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class ForestGuardian extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Forest Guardian";

  public ForestGuardian(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Forest Guardian",
        "${monster.forest_guardian}",
        3259,
        0,
        0,
        0,
        135,
        306,
        30000L,
        "TreeEnt#i",
        "TreeEntA#i",
        "TreeEntC#j",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(new MonsterDef.LootDrop("Light healing potion", 0.15f)),
        false,
        0.0f,
        85,
        74,
        74,
        285,
        0,
        74,
        0,
        new int[] {62, 62, 62, 62, 62, 5000, 100, 100, 100, 100, 100, 100},
        100,
        410,
        0,
        1078525952,
        20018,
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
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d172+134", 1210, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10091, 6, 25),
            new MonsterDef.Attack("", 0, 50, 10120, 6, 25),
            new MonsterDef.Attack("", 0, 50, 10120, 1, 5)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
