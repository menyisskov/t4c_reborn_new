package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class SKRAUGGRUBBRINGAH extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGGRUBBRINGAH(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGGRUBBRINGAH",
        "${monster.skrauggrubbringah}",
        906,
        0,
        38,
        17480,
        45,
        102,
        30000L,
        "64kSkavenPeon#i",
        "64kSkavenPeonA#j",
        "64kSkavenPeonC#t",
        "Skraug Attack.wav",
        "Skraug Die.wav",
        "Skraug Hit.wav",
        75,
        231,
        java.util.List.of(),
        false,
        0.0f,
        57,
        52,
        52,
        65,
        52,
        52,
        23,
        new int[] {74, 74, 74, 74, 74, 5000, 100, 100, 100, 100, 100, 100},
        42,
        178,
        0,
        1101529088,
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
        java.util.List.of(new MonsterDef.Attack("1d58+44", 514, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
