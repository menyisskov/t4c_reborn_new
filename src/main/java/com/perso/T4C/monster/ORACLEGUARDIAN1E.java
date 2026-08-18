package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "ORACLEGUARDIAN1E", x = 2836, y = 2328, z = 2, stationary = false, aggressive = true)
@Spawn(type = "ORACLEGUARDIAN1E", x = 2860, y = 2304, z = 2, stationary = false, aggressive = true)
public final class ORACLEGUARDIAN1E extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public ORACLEGUARDIAN1E(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "ORACLEGUARDIAN1E",
        "${monster.oracleguardian1e}",
        4677,
        0,
        50,
        118327,
        169,
        383,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        224,
        687,
        java.util.List.of(),
        false,
        0.0f,
        140,
        127,
        127,
        165,
        127,
        127,
        40,
        new int[] {31, 63, 31, 63, 47, 5000, 100, 100, 100, 100, 100, 100},
        125,
        510,
        0,
        1115160576,
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
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d215+168", 1510, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
