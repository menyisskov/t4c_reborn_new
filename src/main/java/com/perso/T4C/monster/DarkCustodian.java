package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DarkCustodian extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Dark Custodian";

  public DarkCustodian(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Dark Custodian",
        "${monster.dark_custodian}",
        1153,
        0,
        5,
        3432,
        60,
        135,
        30000L,
        "TreeEnt#i",
        "TreeEntA#i",
        "TreeEntC#j",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        89,
        275,
        java.util.List.of(
            new MonsterDef.LootDrop("Drachensword", 0.003f),
            new MonsterDef.LootDrop("Serious healing potion", 0.02f),
            new MonsterDef.LootDrop("Mana elixir", 0.02f),
            new MonsterDef.LootDrop("Scroll of resist fire", 0.01f),
            new MonsterDef.LootDrop("Scroll of resist ice", 0.01f),
            new MonsterDef.LootDrop("Scroll of protection", 0.01f)),
        false,
        0.0f,
        65,
        59,
        59,
        75,
        0,
        59,
        0,
        new int[] {93, 93, 93, 93, 93, 5000, 100, 100, 100, 100, 100, 100},
        50,
        75,
        0,
        1079083008,
        20018,
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
            new MonsterDef.Attack("1d76+59", 520, 90, 0, 0, 1),
            new MonsterDef.Attack("", 0, 25, 10653, 2, 15),
            new MonsterDef.Attack("", 0, 65, 10094, 2, 15),
            new MonsterDef.Attack("", 0, 10, 10628, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
