package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r207SandWorm extends DataMonster {
  public static final String SOUND_ATTACK = "Worm Attack.wav";
  public static final String SOUND_DEATH = "Worm Dying.wav";
  public static final String SOUND_HIT = "Worm Hit.wav";

  public r207SandWorm(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Sand Worm",
        "${monster.sand_worm}",
        533,
        0,
        3,
        1004,
        25,
        58,
        30000L,
        "SmallWorm#m",
        "SmallWormA#k",
        "SmallWormC#k",
        "Worm Attack.wav",
        "Worm Dying.wav",
        "Worm Hit.wav",
        50,
        154,
        java.util.List.of(),
        false,
        0.0f,
        43,
        40,
        40,
        48,
        0,
        40,
        0,
        new int[] {80, 80, 53, 107, 80, 5000, 100, 100, 100, 100, 100, 100},
        28,
        92,
        0,
        1077149696,
        20016,
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
        java.util.List.of(new MonsterDef.Attack("1d34+24", 346, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
