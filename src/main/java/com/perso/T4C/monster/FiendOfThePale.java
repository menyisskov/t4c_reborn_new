package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class FiendOfThePale extends DataMonster {
  public static final String SOUND_ATTACK = "Demon Attack.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

  public static final String CANONICAL_NAME = "Fiend Of The Pale";

  public FiendOfThePale(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Fiend Of The Pale",
        "${monster.fiend_of_the_pale}",
        2527,
        0,
        8,
        10634,
        115,
        260,
        30000L,
        "Demon#i",
        "DemonA#i",
        "DemonC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        152,
        467,
        java.util.List.of(
            new MonsterDef.LootDrop("Key of the Banished", 0.05f),
            new MonsterDef.LootDrop("Mana elixir", 0.01f),
            new MonsterDef.LootDrop("High metal hand axe 1", 0.0025f),
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f)),
        false,
        0.0f,
        100,
        91,
        91,
        117,
        0,
        91,
        0,
        new int[] {86, 86, 86, 86, 86, 5000, 100, 100, 100, 100, 100, 100},
        85,
        350,
        0,
        1078263808,
        20013,
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
            new MonsterDef.Attack("1d146+114", 1030, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10375, 1, 10),
            new MonsterDef.Attack("", 0, 5, 10381, 1, 10),
            new MonsterDef.Attack("", 0, 50, 10374, 1, 10),
            new MonsterDef.Attack("", 0, 5, 10355, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10314, 1, 10),
            new MonsterDef.Attack("", 0, 5, 10357, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
