package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class SKRAUGSTANDANBONKAH extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGSTANDANBONKAH(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGSTANDANBONKAH",
        "${monster.skraugstandanbonkah}",
        1497,
        0,
        48,
        36522,
        70,
        159,
        30000L,
        "64kSkavenPeon#i",
        "64kSkavenPeonA#j",
        "64kSkavenPeonC#t",
        "Skraug Attack.wav",
        "Skraug Die.wav",
        "Skraug Hit.wav",
        107,
        330,
        java.util.List.of(),
        false,
        0.0f,
        75,
        68,
        68,
        87,
        68,
        68,
        27,
        new int[] {65, 65, 65, 65, 65, 5000, 100, 100, 100, 100, 100, 100},
        60,
        250,
        0,
        1106247680,
        20059,
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
        java.util.List.of(new MonsterDef.Attack("1d90+69", 730, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
