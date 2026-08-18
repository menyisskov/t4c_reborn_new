package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class MOBCELESTIALCOBRA extends DataMonster {
  public static final String SOUND_ATTACK = "Snake Attack.wav";
  public static final String SOUND_DEATH = "Snake Dying.wav";
  public static final String SOUND_HIT = "Snake Hit.wav";

  public MOBCELESTIALCOBRA(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBCELESTIALCOBRA",
        "${monster.mobcelestialcobra}",
        2761,
        0,
        43,
        59738,
        121,
        276,
        30000L,
        "Snake#h",
        "SnakeA#g",
        "SnakeC#m",
        "Snake Attack.wav",
        "Snake Dying.wav",
        "Snake Hit.wav",
        161,
        495,
        java.util.List.of(),
        false,
        0.0f,
        105,
        95,
        95,
        123,
        95,
        95,
        33,
        new int[] {61, 61, 41, 82, 61, 5000, 100, 100, 100, 100, 100, 100},
        90,
        370,
        0,
        1110704128,
        20019,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        75,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d156+120", 1090, 10, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
