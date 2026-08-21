package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class r279Zzartgax extends DataMonster {
  public static final String SOUND_ATTACK = "Atrocity Attack.wav";
  public static final String SOUND_DEATH = "Atrocity Dying.wav";
  public static final String SOUND_HIT = "Atrocity Hit.wav";

  public r279Zzartgax(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Zzartgax",
        "${monster.zzartgax}",
        1321,
        0,
        6,
        8125,
        63,
        143,
        30000L,
        "Tank#h",
        "TankA#h",
        "TankC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        196,
        604,
        java.util.List.of(),
        false,
        0.0f,
        70,
        64,
        64,
        81,
        0,
        64,
        0,
        new int[] {68, 68, 68, 68, 68, 5000, 100, 100, 100, 100, 100, 100},
        55,
        230,
        0,
        1077608448,
        20037,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d81+62", 670, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10091, 3, 10),
            new MonsterDef.Attack("", 0, 50, 10119, 3, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
