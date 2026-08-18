package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DrainSpider extends DataMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public static final String CANONICAL_NAME = "Drain Spider";

  public DrainSpider(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Drain Spider",
        "${monster.drain_spider}",
        1153,
        0,
        6,
        3462,
        60,
        135,
        30000L,
        "Spider#f",
        "SpiderA#f",
        "SpiderC#m",
        "Spider Attack.wav",
        "Spider Dying.wav",
        "Spider Hit.wav",
        89,
        275,
        java.util.List.of(
            new MonsterDef.LootDrop("Drachenrobe", 0.005f),
            new MonsterDef.LootDrop("Bow of the Spiders", 0.02f),
            new MonsterDef.LootDrop("Raincloak", 0.02f),
            new MonsterDef.LootDrop("Mana elixir", 0.02f),
            new MonsterDef.LootDrop("Mana elixir", 0.02f),
            new MonsterDef.LootDrop("Scroll of orientation middle", 0.01f)),
        false,
        0.0f,
        65,
        59,
        59,
        75,
        0,
        59,
        0,
        new int[] {93, 46, 70, 70, 70, 5000, 100, 100, 100, 100, 100, 100},
        50,
        210,
        0,
        1077477376,
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
        35,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d76+59", 610, 75, 0, 0, 1),
            new MonsterDef.Attack("", 0, 25, 10654, 0, 1),
            new MonsterDef.Attack("", 0, 10, 10652, 2, 15),
            new MonsterDef.Attack("", 0, 10, 10628, 2, 15),
            new MonsterDef.Attack("", 0, 80, 10091, 2, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
