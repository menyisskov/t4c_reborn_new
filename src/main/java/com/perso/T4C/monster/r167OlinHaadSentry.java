package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Olin Haad Sentry", x = 2776, y = 1204, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Sentry", x = 2778, y = 1207, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Sentry", x = 2779, y = 1204, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Sentry", x = 2780, y = 1201, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Sentry", x = 2782, y = 1208, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Sentry", x = 2784, y = 1205, z = 0, stationary = false, aggressive = true)
public final class r167OlinHaadSentry extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public r167OlinHaadSentry(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Sentry",
        "${monster.olin_haad_sentry}",
        581,
        0,
        3,
        1154,
        28,
        63,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        53,
        165,
        java.util.List.of(),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {79, 79, 79, 79, 53, 5000, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1076756480,
        10011,
        40030,
        40595,
        40200,
        40201,
        40202,
        40062,
        40175,
        40215,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d36+27", 370, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
