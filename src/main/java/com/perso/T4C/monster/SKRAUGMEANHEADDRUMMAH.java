package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class SKRAUGMEANHEADDRUMMAH extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGMEANHEADDRUMMAH(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGMEANHEADDRUMMAH",
        "${monster.skraugmeanheaddrummah}",
        2129,
        0,
        59,
        63618,
        102,
        233,
        30000L,
        "64kSkavenWarrior#i",
        "64kSkavenWarriorA#k",
        "64kSkavenWarriorC#s",
        "Skraug Attack.wav",
        "Skraug Die.wav",
        "Skraug Hit.wav",
        136,
        418,
        java.util.List.of(),
        false,
        0.0f,
        91,
        83,
        83,
        106,
        83,
        83,
        30,
        new int[] {58, 58, 58, 58, 58, 5000, 100, 100, 100, 100, 100, 100},
        76,
        314,
        0,
        1108869120,
        20062,
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
        java.util.List.of(new MonsterDef.Attack("1d132+101", 922, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
