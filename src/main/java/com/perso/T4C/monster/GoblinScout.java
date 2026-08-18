package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class GoblinScout extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public static final String CANONICAL_NAME = "Goblin Scout";

  public GoblinScout(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Goblin Scout",
        "${monster.goblin_scout}",
        132,
        0,
        2,
        136,
        7,
        17,
        30000L,
        "Goblin#l",
        "GoblinA#i",
        "GoblinC#o",
        "Taunting Attack.wav",
        "Taunting Dying.wav",
        "Taunting Hit.wav",
        14,
        44,
        java.util.List.of(
            new MonsterDef.LootDrop("Goblin leather armor", 0.004f),
            new MonsterDef.LootDrop("Leather belt", 0.01f),
            new MonsterDef.LootDrop("Light healing potion", 0.05f),
            new MonsterDef.LootDrop("Ring of light", 0.01f),
            new MonsterDef.LootDrop("Healing potion", 0.01f)),
        false,
        0.0f,
        23,
        22,
        22,
        24,
        0,
        22,
        0,
        new int[] {120, 60, 90, 90, 90, 5025, 100, 100, 100, 100, 100, 100},
        8,
        42,
        0,
        1074790400,
        20001,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        40,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d11+6", 106, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
