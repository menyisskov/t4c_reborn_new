package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r149MOONTUGGUARD21 extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public r149MOONTUGGUARD21(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOONTUGGUARD21",
        "${monster.moontugguard21}",
        32590,
        0,
        4,
        74158,
        135,
        306,
        30000L,
        "64kSkavenWarrior#i",
        "64kSkavenWarriorA#k",
        "64kSkavenWarriorC#s",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        189,
        550,
        java.util.List.of(),
        false,
        0.0f,
        115,
        104,
        104,
        135,
        104,
        104,
        35,
        new int[] {47, 47, 47, 47, 47, 5000, 100, 100, 100, 100, 100, 100},
        100,
        410,
        0,
        1112014848,
        20062,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d172+134", 1210, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
