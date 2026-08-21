package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class HiveDefender extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public static final String CANONICAL_NAME = "Hive Defender";

  public HiveDefender(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Hive Defender",
        "${monster.hive_defender}",
        1321,
        0,
        6,
        4062,
        63,
        143,
        30000L,
        "GiantWasp#h",
        "GiantWaspA#h",
        "GiantWaspC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        98,
        302,
        java.util.List.of(
            new MonsterDef.LootDrop("Wasp wax", 0.005f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f)),
        false,
        0.0f,
        70,
        64,
        64,
        81,
        0,
        64,
        0,
        new int[] {45, 90, 68, 68, 68, 5000, 100, 100, 100, 100, 100, 100},
        55,
        230,
        0,
        1077608448,
        20029,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        32,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d81+62", 670, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 85, 10091, 2, 12),
            new MonsterDef.Attack("", 0, 5, 10347, 2, 12),
            new MonsterDef.Attack("", 0, 5, 10344, 2, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
