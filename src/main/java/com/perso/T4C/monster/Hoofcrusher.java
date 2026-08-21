package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class Hoofcrusher extends DataMonster {
  public static final String SOUND_ATTACK = "Minotaur Attack.wav";
  public static final String SOUND_DEATH = "Minotaur Dying.wav";
  public static final String SOUND_HIT = "Minotaur Hit.wav";

  public static final String CANONICAL_NAME = "Hoofcrusher";

  public Hoofcrusher(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Hoofcrusher",
        "${monster.hoofcrusher}",
        1752,
        0,
        5,
        4406,
        43,
        98,
        30000L,
        "Minotaur#f",
        "MinotaurA#i",
        "MinotaurC!m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        146,
        450,
        java.util.List.of(
            new MonsterDef.LootDrop("Manastone", 0.02f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Healing potion", 0.05f),
            new MonsterDef.LootDrop("Sapphire bracelet", 0.01f),
            new MonsterDef.LootDrop("Minotaur clan ring", 0.005f)),
        false,
        0.0f,
        56,
        51,
        51,
        64,
        0,
        51,
        0,
        new int[] {99, 49, 74, 74, 74, 5000, 100, 100, 100, 100, 100, 100},
        41,
        174,
        0,
        1077149696,
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
        java.util.List.of(new MonsterDef.Attack("1d56+42", 502, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
