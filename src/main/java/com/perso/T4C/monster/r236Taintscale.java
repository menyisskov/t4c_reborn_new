package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r236Taintscale extends DataMonster {
  public static final String SOUND_ATTACK = "Snake Attack.wav";
  public static final String SOUND_DEATH = "Snake Dying.wav";
  public static final String SOUND_HIT = "Snake Hit.wav";

  public r236Taintscale(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Taintscale",
        "${monster.taintscale}",
        293,
        0,
        2,
        410,
        14,
        33,
        30000L,
        "Snake#h",
        "SnakeA#g",
        "SnakeC#m",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        30,
        93,
        java.util.List.of(),
        false,
        0.0f,
        32,
        30,
        30,
        35,
        0,
        30,
        0,
        new int[] {86, 86, 57, 114, 86, 5000, 100, 100, 100, 100, 100, 100},
        17,
        78,
        0,
        1075838976,
        20019,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d20+13", 214, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 75, 10119, 1, 10),
            new MonsterDef.Attack("", 0, 25, 10747, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
