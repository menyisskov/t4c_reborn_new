package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class SKRAUGCLANGBANGAH extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGCLANGBANGAH(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGCLANGBANGAH",
        "${monster.skraugclangbangah}",
        1723,
        0,
        51,
        44808,
        79,
        180,
        30000L,
        "64kSkavenSkavenger#i",
        "64kSkavenSkavengerA#i",
        "64kSkavenSkavengerC#s",
        "Skraug Attack.wav",
        "Skraug Die.wav",
        "Skraug Hit.wav",
        118,
        363,
        java.util.List.of(),
        false,
        0.0f,
        81,
        74,
        74,
        94,
        74,
        74,
        28,
        new int[] {62, 62, 62, 62, 62, 5000, 100, 100, 100, 100, 100, 100},
        66,
        274,
        0,
        1107558400,
        20061,
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
        java.util.List.of(new MonsterDef.Attack("1d102+78", 802, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
