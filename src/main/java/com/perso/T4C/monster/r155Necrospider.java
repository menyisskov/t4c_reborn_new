package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class r155Necrospider extends DataMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public r155Necrospider(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Necrospider",
        "${monster.necrospider}",
        1684,
        0,
        6,
        5780,
        78,
        176,
        30000L,
        "Spider#f",
        "SpiderA#f",
        "SpiderC!m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        116,
        357,
        java.util.List.of(
            new MonsterDef.LootDrop("Spider venom", 0.03f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f)),
        false,
        0.0f,
        80,
        73,
        73,
        93,
        0,
        73,
        0,
        new int[] {84, 42, 63, 63, 63, 5000, 100, 100, 100, 100, 100, 100},
        65,
        270,
        0,
        1077936128,
        20007,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        31,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d99+77", 790, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 45, 10119, 1, 12),
            new MonsterDef.Attack("", 0, 10, 10371, 2, 12),
            new MonsterDef.Attack("", 0, 45, 10344, 1, 12),
            new MonsterDef.Attack("", 0, 3, 10317, 1, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
