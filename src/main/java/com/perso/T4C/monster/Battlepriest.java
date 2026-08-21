package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class Battlepriest extends DataMonster {
  public static final String SOUND_ATTACK = null;
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Battle priest";

  public Battlepriest(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Battle priest",
        "${monster.battle_priest}",
        235,
        0,
        2,
        298,
        11,
        27,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        25,
        77,
        java.util.List.of(
            new MonsterDef.LootDrop("Mace of strength", 0.05f),
            new MonsterDef.LootDrop("Ring of the healer", 0.0075f),
            new MonsterDef.LootDrop("Potion of lesser protection from evil", 0.02f),
            new MonsterDef.LootDrop("Leather armor", 0.01f),
            new MonsterDef.LootDrop("Potion of cure poison", 0.02f)),
        false,
        0.0f,
        29,
        27,
        27,
        31,
        0,
        27,
        0,
        new int[] {87, 87, 87, 87, 116, 5000, 100, 100, 100, 100, 100, 100},
        14,
        66,
        0,
        1075576832,
        10009,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        46,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d17+10", 215, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
