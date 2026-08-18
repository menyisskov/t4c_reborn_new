package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Kraanian Reaper", x = 2107, y = 2204, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Reaper", x = 2117, y = 2212, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Reaper", x = 2119, y = 2202, z = 1, stationary = false, aggressive = true)
public final class KraanianReaper extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Kraanian Reaper";

  public KraanianReaper(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Kraanian Reaper",
        "${monster.kraanian_reaper}",
        1801,
        0,
        7,
        6369,
        82,
        187,
        30000L,
        "Kraanian#h",
        "KraanianA#h",
        "KraanianC#l",
        "Kraanian Attack.wav",
        "Kraanian Dying.wav",
        "Kraanian Hit.wav",
        122,
        374,
        java.util.List.of(),
        false,
        0.0f,
        83,
        76,
        76,
        96,
        0,
        76,
        0,
        new int[] {82, 41, 62, 62, 62, 5000, 100, 100, 100, 100, 100, 100},
        68,
        282,
        0,
        1078001664,
        20025,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d106+81", 826, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
