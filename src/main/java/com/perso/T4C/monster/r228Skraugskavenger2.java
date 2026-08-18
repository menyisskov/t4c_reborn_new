package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r228Skraugskavenger2 extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public r228Skraugskavenger2(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skraug skavenger 2",
        "${monster.skraug_skavenger_2}",
        1000,
        0,
        0,
        0,
        1,
        10,
        30000L,
        "64kSkavenSkavenger#i",
        "64kSkavenSkavengerA#i",
        "64kSkavenSkavengerC#s",
        "Skraug Attack.wav",
        "Skraug Die.wav",
        "Skraug Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        100,
        100,
        100,
        100,
        0,
        100,
        0,
        new int[] {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
        100,
        0,
        0,
        1076101120,
        20061,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        21,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d10", 100, 0, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
