package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r145MOBQUENTINTARANTULA1 extends DataMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public r145MOBQUENTINTARANTULA1(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBQUENTINTARANTULA1",
        "${monster.mobquentintarantula1}",
        3006,
        0,
        44,
        66721,
        128,
        291,
        30000L,
        "Tarantula#m",
        "Tarantula#m",
        "TarantulaC#n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
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
        new int[] {78, 39, 58, 58, 58, 5000, 100, 100, 100, 100, 100, 100},
        95,
        390,
        0,
        1111228416,
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
        java.util.List.of(new MonsterDef.Attack("1d164+127", 1150, 10, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
