package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class FoulFiend extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Foul Fiend";

  public FoulFiend(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Foul Fiend",
        "${monster.foul_fiend}",
        2390,
        0,
        8,
        9876,
        110,
        251,
        30000L,
        "AtrocityBoss#h",
        "AtrocityBossA#h",
        "AtrocityBossC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        147,
        451,
        java.util.List.of(
            new MonsterDef.LootDrop("Mana elixir", 0.01f),
            new MonsterDef.LootDrop("Rough carnelian", 0.02f),
            new MonsterDef.LootDrop("Rough ruby", 0.004f),
            new MonsterDef.LootDrop("Rough garnet", 0.001f)),
        false,
        0.0f,
        97,
        88,
        88,
        113,
        0,
        88,
        0,
        new int[] {66, 66, 44, 88, 66, 5000, 100, 100, 100, 100, 100, 100},
        82,
        338,
        0,
        1078231040,
        20040,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        37,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d142+109", 994, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10314, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10376, 1, 10),
            new MonsterDef.Attack("", 0, 40, 10090, 1, 10),
            new MonsterDef.Attack("", 0, 40, 10086, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
