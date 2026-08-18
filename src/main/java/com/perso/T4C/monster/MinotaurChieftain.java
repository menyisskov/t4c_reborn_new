package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class MinotaurChieftain extends DataMonster {
  public static final String SOUND_ATTACK = "Minotaur Attack.wav";
  public static final String SOUND_DEATH = "Minotaur Dying.wav";
  public static final String SOUND_HIT = "Minotaur Hit.wav";

  public static final String CANONICAL_NAME = "Minotaur Chieftain";

  public MinotaurChieftain(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Minotaur Chieftain",
        "${monster.minotaur_chieftain}",
        710,
        0,
        4,
        1575,
        34,
        78,
        30000L,
        "Minotaur#f",
        "MinotaurA#i",
        "MinotaurC#m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        62,
        192,
        java.util.List.of(
            new MonsterDef.LootDrop("Potion of fury", 0.008f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Fine steel long sword", 0.01f),
            new MonsterDef.LootDrop("Pouch of Blue Cohosh", 0.01f),
            new MonsterDef.LootDrop("Minotaur clan ring", 0.005f),
            new MonsterDef.LootDrop("Flask of bluish liquid", 0.01f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Wooden shield", 0.01f)),
        false,
        0.0f,
        50,
        46,
        46,
        57,
        0,
        46,
        0,
        new int[] {103, 51, 77, 77, 77, 5000, 100, 100, 100, 100, 100, 100},
        35,
        150,
        0,
        1076953088,
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
            new MonsterDef.Attack("1d45+33", 430, 100, 0, 0, 0),
            new MonsterDef.Attack("1d59+39", 342, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10086, 3, 12),
            new MonsterDef.Attack("", 0, 50, 10119, 3, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
