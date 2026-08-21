package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Time Guardian", x = 2776, y = 2324, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2788, y = 2336, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2790, y = 2310, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2796, y = 2316, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2802, y = 2322, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2804, y = 2296, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2816, y = 2308, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2828, y = 2272, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2840, y = 2284, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2842, y = 2258, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2848, y = 2264, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2854, y = 2270, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2856, y = 2244, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Guardian", x = 2868, y = 2256, z = 2, stationary = false, aggressive = true)
public final class r244TimeGuardian extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public r244TimeGuardian(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Time Guardian",
        "${monster.time_guardian}",
        5641,
        0,
        10,
        30115,
        149,
        389,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        251,
        770,
        java.util.List.of(),
        false,
        0.0f,
        155,
        140,
        140,
        183,
        0,
        140,
        0,
        new int[] {47, 63, 47, 63, 63, 5000, 100, 100, 100, 100, 100, 100},
        140,
        570,
        0,
        1079083008,
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
        1,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d241+148", 1690, 100, 0, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
