package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r166OlinHaadPrivateGuard extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r166OlinHaadPrivateGuard(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Private Guard",
        "${monster.olin_haad_private_guard}",
        463,
        0,
        3,
        810,
        22,
        50,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        44,
        137,
        java.util.List.of(),
        false,
        0.0f,
        40,
        37,
        37,
        45,
        0,
        37,
        0,
        new int[] {82, 82, 82, 82, 54, 5000, 100, 100, 100, 100, 100, 100},
        25,
        110,
        0,
        1076363264,
        10011,
        40207,
        40203,
        40204,
        40205,
        40206,
        40061,
        40249,
        40215,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d29+21", 310, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
