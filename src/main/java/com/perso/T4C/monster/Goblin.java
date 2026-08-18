package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Goblin extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public static final String CANONICAL_NAME = "Goblin";

  public Goblin(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Goblin",
        "${monster.goblin}",
        84,
        0,
        1,
        77,
        5,
        12,
        30000L,
        "Goblin#l",
        "GoblinA#i",
        "GoblinC#o",
        "Taunting Attack.wav",
        "Taunting Dying.wav",
        "Taunting Hit.wav",
        8,
        27,
        java.util.List.of(
            new MonsterDef.LootDrop("Goblin leather armor", 0.005f),
            new MonsterDef.LootDrop("Iron ring", 0.01f),
            new MonsterDef.LootDrop("Light healing potion", 0.06f),
            new MonsterDef.LootDrop("Healing potion", 0.02f)),
        false,
        0.0f,
        20,
        19,
        19,
        21,
        0,
        19,
        0,
        new int[] {121, 60, 91, 91, 91, 5025, 100, 100, 100, 100, 100, 100},
        5,
        30,
        0,
        1073741824,
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
        java.util.List.of(new MonsterDef.Attack("1d8+4", 70, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
