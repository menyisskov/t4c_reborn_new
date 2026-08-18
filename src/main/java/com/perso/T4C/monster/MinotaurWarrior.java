package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class MinotaurWarrior extends DataMonster {
  public static final String SOUND_ATTACK = "Minotaur Attack.wav";
  public static final String SOUND_DEATH = "Minotaur Dying.wav";
  public static final String SOUND_HIT = "Minotaur Hit.wav";

  public static final String CANONICAL_NAME = "Minotaur Warrior";

  public MinotaurWarrior(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Minotaur Warrior",
        "${monster.minotaur_warrior}",
        581,
        0,
        3,
        1154,
        28,
        63,
        30000L,
        "Minotaur#f",
        "MinotaurA#i",
        "MinotaurC#m",
        "Minotaur Attack.wav",
        "Minotaur Dying.wav",
        "Minotaur Hit.wav",
        53,
        165,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Healing potion", 0.02f),
            new MonsterDef.LootDrop("Potion of fury", 0.01f),
            new MonsterDef.LootDrop("Ring of the bear", 0.01f),
            new MonsterDef.LootDrop("Blue gem", 0.008f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.001f),
            new MonsterDef.LootDrop("Dead Fishes", 0.008f),
            new MonsterDef.LootDrop("Flask of bluish liquid", 0.01f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {106, 53, 79, 79, 79, 5000, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1076756480,
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
        java.util.List.of(new MonsterDef.Attack("1d36+27", 370, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
