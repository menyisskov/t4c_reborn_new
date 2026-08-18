package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class HowlingTerror extends DataMonster {
  public static final String SOUND_ATTACK = "Taunting Attack.wav";
  public static final String SOUND_DEATH = "Taunting Dying.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public static final String CANONICAL_NAME = "Howling Terror";

  public HowlingTerror(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Howling Terror",
        "${monster.howling_terror}",
        1219,
        0,
        5,
        3614,
        59,
        133,
        30000L,
        "Taunting#h",
        "TauntingA#h",
        "TauntingC#m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        93,
        286,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough moonstone", 0.001f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough agate", 0.02f),
            new MonsterDef.LootDrop("Blackened iron key", 0.005f)),
        false,
        0.0f,
        67,
        61,
        61,
        77,
        0,
        61,
        0,
        new int[] {55, 111, 82, 82, 82, 5000, 100, 100, 100, 100, 100, 100},
        52,
        218,
        0,
        1077542912,
        20038,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        34,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d75+58", 634, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 40, 10091, 3, 10),
            new MonsterDef.Attack("", 0, 25, 10119, 3, 10),
            new MonsterDef.Attack("", 0, 5, 10378, 3, 10),
            new MonsterDef.Attack("", 0, 20, 10384, 3, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
