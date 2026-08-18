package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class MinotaurShaman extends DataMonster {
  public static final String SOUND_ATTACK = "Minotaur Attack.wav";
  public static final String SOUND_DEATH = "Minotaur Dying.wav";
  public static final String SOUND_HIT = "Minotaur Hit.wav";

  public static final String CANONICAL_NAME = "Minotaur Shaman";

  public MinotaurShaman(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Minotaur Shaman",
        "${monster.minotaur_shaman}",
        792,
        0,
        4,
        1864,
        38,
        87,
        30000L,
        "Minotaur#f",
        "MinotaurA#i",
        "MinotaurC#m",
        "Minotaur Attack.wav",
        "Minotaur Dying.wav",
        "Minotaur Hit.wav",
        68,
        209,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Manastone", 0.03f),
            new MonsterDef.LootDrop("Sapphire bracelet", 0.01f),
            new MonsterDef.LootDrop("Dead Fishes", 0.01f),
            new MonsterDef.LootDrop("Pouch of Blue Cohosh", 0.03f),
            new MonsterDef.LootDrop("Pouch of Black Snakeroot", 0.02f),
            new MonsterDef.LootDrop("Pouch of Woody Nightshade", 0.01f),
            new MonsterDef.LootDrop("Rune etched robe of protection", 0.002f)),
        false,
        0.0f,
        53,
        49,
        49,
        60,
        0,
        49,
        0,
        new int[] {101, 50, 76, 76, 76, 5000, 100, 100, 100, 100, 100, 100},
        38,
        162,
        0,
        1077084160,
        20014,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d50+37", 466, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 70, 10120, 10, 15),
            new MonsterDef.Attack("", 0, 30, 10086, 10, 15),
            new MonsterDef.Attack("", 0, 30, 10096, 2, 9)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
