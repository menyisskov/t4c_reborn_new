package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class SKRAUGBIGWORVIKTOR extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGBIGWORVIKTOR(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGBIGWORVIKTOR",
        "${monster.skraugbigworviktor}",
        2301,
        0,
        40,
        47045,
        108,
        245,
        30000L,
        "64kSkavenWarrior#i",
        "64kSkavenWarriorA#k",
        "64kSkavenWarriorC#s",
        "Skraug Attack.wav",
        "Skraug Die.wav",
        "Skraug Hit.wav",
        143,
        440,
        java.util.List.of(),
        false,
        0.0f,
        95,
        86,
        86,
        111,
        86,
        86,
        31,
        new int[] {56, 56, 56, 56, 56, 5000, 100, 100, 100, 100, 100, 100},
        150,
        330,
        0,
        1109393408,
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
        java.util.List.of(new MonsterDef.Attack("1d138+107", 970, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
