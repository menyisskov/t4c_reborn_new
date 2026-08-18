package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class SKRAUGBLUDFIGHTOR extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGBLUDFIGHTOR(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGBLUDFIGHTOR",
        "${monster.skraugbludfightor}",
        1801,
        0,
        35,
        31845,
        82,
        187,
        30000L,
        "64kSkavenSkavenger#i",
        "64kSkavenSkavengerA#i",
        "64kSkavenSkavengerC#s",
        "Skraug Attack.wav",
        "Skraug Die.wav",
        "Skraug Hit.wav",
        122,
        374,
        java.util.List.of(),
        false,
        0.0f,
        83,
        76,
        76,
        96,
        76,
        76,
        28,
        new int[] {62, 62, 62, 62, 62, 5000, 100, 100, 100, 100, 100, 100},
        150,
        282,
        0,
        1107820544,
        20049,
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
        java.util.List.of(new MonsterDef.Attack("1d106+81", 826, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
