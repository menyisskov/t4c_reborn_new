package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class SKRAUGPROTEKTERRITOR extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGPROTEKTERRITOR(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGPROTEKTERRITOR",
        "${monster.skraugprotekterritor}",
        1426,
        0,
        31,
        22704,
        67,
        153,
        30000L,
        "64kSkavenPeon#i",
        "64kSkavenPeonA#j",
        "64kSkavenPeonC#t",
        "Skraug Attack.wav",
        "Skraug Die.wav",
        "Skraug Hit.wav",
        104,
        319,
        java.util.List.of(),
        false,
        0.0f,
        73,
        67,
        67,
        84,
        67,
        67,
        26,
        new int[] {66, 66, 66, 66, 66, 5000, 100, 100, 100, 100, 100, 100},
        150,
        242,
        0,
        1105723392,
        20047,
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
        java.util.List.of(new MonsterDef.Attack("1d87+66", 706, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
