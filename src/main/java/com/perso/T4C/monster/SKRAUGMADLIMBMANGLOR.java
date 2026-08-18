package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class SKRAUGMADLIMBMANGLOR extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGMADLIMBMANGLOR(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGMADLIMBMANGLOR",
        "${monster.skraugmadlimbmanglor}",
        2044,
        0,
        37,
        38173,
        91,
        208,
        30000L,
        "64kSkavenWarrior#i",
        "64kSkavenWarriorA#k",
        "64kSkavenWarriorC#s",
        "Skraug Attack.wav",
        "Skraug Die.wav",
        "Skraug Hit.wav",
        133,
        407,
        java.util.List.of(),
        false,
        0.0f,
        89,
        81,
        81,
        103,
        81,
        81,
        29,
        new int[] {59, 59, 59, 59, 59, 5000, 100, 100, 100, 100, 100, 100},
        150,
        306,
        0,
        1108606976,
        20050,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d118+90", 898, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
