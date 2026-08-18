package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class SKRAUGTOTEMHERBDOKTOR extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGTOTEMHERBDOKTOR(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGTOTEMHERBDOKTOR",
        "${monster.skraugtotemherbdoktor}",
        2086,
        0,
        37,
        39279,
        93,
        211,
        30000L,
        "64kSkavenShaman#j",
        "64kSkavenShamanA#j",
        "64kSkavenShamanC#s",
        "Skraug Attack.wav",
        "Skraug Die.wav",
        "Skraug Hit.wav",
        134,
        412,
        java.util.List.of(),
        false,
        0.0f,
        90,
        82,
        82,
        105,
        82,
        82,
        30,
        new int[] {58, 58, 58, 58, 58, 5000, 100, 100, 100, 100, 100, 100},
        150,
        310,
        0,
        1108606976,
        20048,
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
        java.util.List.of(new MonsterDef.Attack("1d119+92", 910, 2, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
