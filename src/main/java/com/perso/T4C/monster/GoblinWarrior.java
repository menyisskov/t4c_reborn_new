package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class GoblinWarrior extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public static final String CANONICAL_NAME = "Goblin Warrior";

  public GoblinWarrior(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Goblin Warrior",
        "${monster.goblin_warrior}",
        199,
        0,
        2,
        235,
        10,
        23,
        30000L,
        "Goblin#l",
        "GoblinA#i",
        "GoblinC#o",
        "Taunting Attack.wav",
        "Taunting Dying.wav",
        "Taunting Hit.wav",
        21,
        66,
        java.util.List.of(
            new MonsterDef.LootDrop("Goblin Blade", 0.004f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Iron key", 0.005f)),
        false,
        0.0f,
        27,
        25,
        25,
        29,
        0,
        25,
        0,
        new int[] {117, 58, 88, 88, 88, 5025, 100, 100, 100, 100, 100, 100},
        12,
        58,
        0,
        1075314688,
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
        java.util.List.of(new MonsterDef.Attack("1d14+9", 154, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
