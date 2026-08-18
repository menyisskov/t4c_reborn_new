package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r159Nightbreed extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r159Nightbreed(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Nightbreed",
        "${monster.nightbreed}",
        440,
        0,
        3,
        758,
        21,
        48,
        30000L,
        "Bat#h",
        "BatA#i",
        "BatC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        43,
        132,
        java.util.List.of(),
        false,
        0.0f,
        39,
        36,
        36,
        43,
        0,
        36,
        0,
        new int[] {55, 110, 82, 82, 82, 5000, 100, 100, 100, 100, 100, 100},
        24,
        166,
        0,
        0,
        20002,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d28+20", 298, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 35, 10094, 5, 15),
            new MonsterDef.Attack("", 0, 60, 10120, 5, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
