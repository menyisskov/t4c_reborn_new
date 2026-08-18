package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class GoblinSubchief extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public static final String CANONICAL_NAME = "Goblin Subchief";

  public GoblinSubchief(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Goblin Subchief",
        "${monster.goblin_subchief}",
        254,
        0,
        2,
        337,
        13,
        29,
        30000L,
        "Goblin#l",
        "GoblinA#i",
        "GoblinC#o",
        "Taunting Attack.wav",
        "Taunting Dying.wav",
        "Taunting Hit.wav",
        26,
        82,
        java.util.List.of(
            new MonsterDef.LootDrop("Ringmail armor", 0.008f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Leather belt", 0.01f),
            new MonsterDef.LootDrop("Light healing potion", 0.05f),
            new MonsterDef.LootDrop("Flask of Goblin Blood", 0.005f)),
        false,
        0.0f,
        32,
        28,
        28,
        33,
        0,
        28,
        0,
        new int[] {115, 57, 86, 86, 86, 5025, 100, 100, 100, 100, 100, 100},
        15,
        70,
        0,
        1075576832,
        20001,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d17+12", 190, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
