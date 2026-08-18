package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r141MOBGAUZECORPSE1 extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public r141MOBGAUZECORPSE1(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBGAUZECORPSE1",
        "${monster.mobgauzecorpse1}",
        3006,
        0,
        44,
        66721,
        128,
        291,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        170,
        522,
        java.util.List.of(),
        false,
        0.0f,
        110,
        100,
        100,
        129,
        100,
        100,
        34,
        new int[] {58, 58, 78, 39, 5025, 49, 100, 100, 100, 100, 100, 100},
        95,
        390,
        0,
        1111228416,
        20011,
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
        java.util.List.of(new MonsterDef.Attack("1d164+127", 1150, 10, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
