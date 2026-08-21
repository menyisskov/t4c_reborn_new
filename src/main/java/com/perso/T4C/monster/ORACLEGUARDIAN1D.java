package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "ORACLEGUARDIAN1D", x = 2800, y = 2340, z = 2, stationary = false, aggressive = true)
@Spawn(type = "ORACLEGUARDIAN1D", x = 2872, y = 2268, z = 2, stationary = false, aggressive = true)
public final class ORACLEGUARDIAN1D extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public ORACLEGUARDIAN1D(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "ORACLEGUARDIAN1D",
        "${monster.oracleguardian1d}",
        4677,
        0,
        50,
        118327,
        169,
        383,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
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
