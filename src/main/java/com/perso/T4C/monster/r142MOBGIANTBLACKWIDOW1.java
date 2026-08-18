package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r142MOBGIANTBLACKWIDOW1 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r142MOBGIANTBLACKWIDOW1(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBGIANTBLACKWIDOW1",
        "${monster.mobgiantblackwidow1}",
        2086,
        0,
        37,
        39279,
        93,
        211,
        30000L,
        "Tarantula#m",
        "Tarantula#m",
        "TarantulaC#n",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        134,
        412,
        java.util.List.of(),
        false,
        0.0f,
        90,
        82,
        82,
        105,
        82,
        82,
        30,
        new int[] {93, 46, 70, 70, 70, 5000, 100, 100, 100, 100, 100, 100},
        75,
        310,
        0,
        1108606976,
        20033,
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
        java.util.List.of(new MonsterDef.Attack("1d119+92", 910, 10, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
