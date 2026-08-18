package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Arachnofiend extends DataMonster {
  public static final String SOUND_ATTACK = "Demon Attack.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

  public static final String CANONICAL_NAME = "Arachnofiend";

  public Arachnofiend(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Arachnofiend",
        "${monster.arachnofiend}",
        1497,
        0,
        6,
        4869,
        70,
        159,
        30000L,
        "Tarantula#m",
        "Tarantula#m",
        "TarantulaC#n",
        "Spider Attack.wav",
        "Spider Dying.wav",
        "Spider Hit.wav",
        107,
        330,
        java.util.List.of(
            new MonsterDef.LootDrop("Tarantula fang", 0.03f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Tarantula eyes", 0.03f)),
        false,
        0.0f,
        75,
        68,
        68,
        87,
        0,
        68,
        0,
        new int[] {87, 43, 65, 65, 65, 5000, 100, 100, 100, 100, 100, 100},
        60,
        250,
        0,
        1077805056,
        20033,
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
            new MonsterDef.Attack("1d90+69", 730, 95, 0, 0, 0),
            new MonsterDef.Attack("", 0, 40, 10344, 1, 10),
            new MonsterDef.Attack("", 0, 55, 10091, 1, 10),
            new MonsterDef.Attack("", 0, 5, 10314, 0, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
