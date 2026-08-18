package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class GoblinWarchief extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public static final String CANONICAL_NAME = "Goblin Warchief";

  public GoblinWarchief(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Goblin Warchief",
        "${monster.goblin_warchief}",
        334,
        0,
        2,
        496,
        16,
        37,
        30000L,
        "Goblin#l",
        "GoblinA#i",
        "GoblinC#o",
        "Taunting Attack.wav",
        "Taunting Dying.wav",
        "Taunting Hit.wav",
        34,
        104,
        java.util.List.of(
            new MonsterDef.LootDrop("Feather", 0.01f),
            new MonsterDef.LootDrop("Golden ring", 0.001f),
            new MonsterDef.LootDrop("Bracelet of power", 5.0E-4f),
            new MonsterDef.LootDrop("Iron key", 0.0069999998f),
            new MonsterDef.LootDrop("Ringmail armor", 5.0E-4f),
            new MonsterDef.LootDrop("Ringmail leggings", 5.0E-4f),
            new MonsterDef.LootDrop("Ringmail helmet", 5.0E-4f),
            new MonsterDef.LootDrop("Flask of Goblin Blood", 0.005f)),
        false,
        0.0f,
        34,
        32,
        32,
        37,
        0,
        32,
        0,
        new int[] {113, 56, 85, 85, 85, 5025, 100, 100, 100, 100, 100, 100},
        19,
        86,
        0,
        1075970048,
        20001,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        70,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d22+15", 238, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
