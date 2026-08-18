package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;

public final class Delwobble extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public Delwobble(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBDELWOBBLE",
        "${monster.mobdelwobble}",
        1420,
        0,
        22,
        15750,
        34,
        78,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        124,
        384,
        java.util.List.of(),
        false,
        0.0f,
        50,
        46,
        46,
        57,
        46,
        46,
        22,
        new int[] {77, 77, 77, 77, 103, 5000, 100, 100, 100, 100, 100, 100},
        35,
        150,
        0,
        1099431936,
        10011,
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
        java.util.List.of(new MonsterDef.Attack("1d45+33", 430, 50, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
