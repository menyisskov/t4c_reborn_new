package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r246TimeElemental extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public r246TimeElemental(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "TimeElemental",
        "${monster.timeelemental}",
        100000,
        0,
        0,
        0,
        0,
        0,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        500,
        500,
        500,
        500,
        0,
        500,
        0,
        new int[] {100, 100, 100, 100, 100, 5000, 100, 100, 100, 100, 100, 100},
        100,
        1000,
        0,
        1079574528,
        20036,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        35,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("", 0, 80, 10086, 1, 15),
            new MonsterDef.Attack("1d100+150", 5000, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 20, 10596, 1, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
