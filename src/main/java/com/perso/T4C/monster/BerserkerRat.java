package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class BerserkerRat extends DataMonster {
  public static final String SOUND_ATTACK = "Rat Attack.wav";
  public static final String SOUND_DEATH = "Rat Dying.wav";
  public static final String SOUND_HIT = "Rat Hit.wav";

  public static final String CANONICAL_NAME = "Berserker Rat";

  public BerserkerRat(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Berserker Rat",
        "${monster.berserker_rat}",
        1571,
        0,
        6,
        5224,
        73,
        166,
        30000L,
        "Rat#f",
        "RatA#i",
        "RatC#j",
        "Rat Attack.wav",
        "Rat Dying.wav",
        "Rat Hit.wav",
        111,
        341,
        java.util.List.of(
            new MonsterDef.LootDrop("Potion of nimbleness", 0.05f),
            new MonsterDef.LootDrop("Rough agate", 0.02f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough moonstone", 0.001f)),
        false,
        0.0f,
        77,
        70,
        70,
        89,
        0,
        70,
        0,
        new int[] {103, 51, 77, 77, 77, 5000, 100, 100, 100, 100, 100, 100},
        62,
        258,
        0,
        1077870592,
        20003,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        43,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d94+72", 754, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10352, 2, 8),
            new MonsterDef.Attack("", 0, 3, 10351, 2, 8),
            new MonsterDef.Attack("", 0, 10, 10355, 9, 18)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
