package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class FoulMuck extends DataMonster {
  public static final String SOUND_ATTACK = "Ooze Attack.wav";
  public static final String SOUND_DEATH = "Ooze Dying.wav";
  public static final String SOUND_HIT = "Ooze Hit.wav";

  public static final String CANONICAL_NAME = "Foul Muck";

  public FoulMuck(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Foul Muck",
        "${monster.foul_muck}",
        1321,
        0,
        6,
        4062,
        63,
        143,
        30000L,
        "Slime@023",
        "Slimea@35",
        "SlimeC!z",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        98,
        302,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f)),
        false,
        0.0f,
        70,
        64,
        64,
        81,
        0,
        64,
        0,
        new int[] {54, 108, 108, 54, 81, 5000, 100, 100, 100, 100, 100, 100},
        55,
        230,
        0,
        1077608448,
        20005,
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
            new MonsterDef.Attack("1d81+62", 670, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 30, 10348, 6, 10),
            new MonsterDef.Attack("", 0, 3, 10391, 6, 10),
            new MonsterDef.Attack("", 0, 25, 10091, 2, 10),
            new MonsterDef.Attack("", 0, 5, 10333, 2, 5),
            new MonsterDef.Attack("", 0, 10, 10371, 2, 5)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
