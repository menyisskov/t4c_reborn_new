package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class SKRAUGMUNCHHUNTOR extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGMUNCHHUNTOR(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGMUNCHHUNTOR",
        "${monster.skraugmunchhuntor}",
        966,
        0,
        26,
        12870,
        48,
        109,
        30000L,
        "64kSkavenPeon#i",
        "64kSkavenPeonA#j",
        "64kSkavenPeonC#t",
        "Skraug Attack.wav",
        "Skraug Die.wav",
        "Skraug Hit.wav",
        89,
        242,
        java.util.List.of(),
        false,
        0.0f,
        59,
        54,
        54,
        67,
        54,
        54,
        23,
        new int[] {73, 73, 73, 73, 73, 5000, 100, 100, 100, 100, 100, 100},
        150,
        186,
        0,
        1102053376,
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
        java.util.List.of(new MonsterDef.Attack("1d62+47", 538, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
