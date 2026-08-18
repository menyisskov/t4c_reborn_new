package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class FallenWarrior extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Fallen Warrior";

  public FallenWarrior(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Fallen Warrior",
        "${monster.fallen_warrior}",
        374,
        0,
        3,
        590,
        18,
        41,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC#j",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        37,
        115,
        java.util.List.of(new MonsterDef.LootDrop("Corrupt heart", 0.1f)),
        false,
        0.0f,
        36,
        33,
        33,
        40,
        0,
        33,
        0,
        new int[] {84, 84, 111, 55, 5025, 55, 100, 100, 100, 100, 100, 100},
        21,
        94,
        0,
        1076101120,
        20009,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d24+17", 262, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
