package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBMERCENARYB", x = 2728, y = 977, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYB", x = 2733, y = 972, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYB", x = 2736, y = 957, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYB", x = 2738, y = 963, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYB", x = 2740, y = 970, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYB", x = 2742, y = 974, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYB", x = 2748, y = 971, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYB", x = 2748, y = 979, z = 0, stationary = false, aggressive = true)
public final class MercenaryB extends DataMonster {

  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public MercenaryB(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBMERCENARYB",
        "${monster.mobmercenaryb}",
        116,
        0,
        9,
        563,
        6,
        15,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        12,
        38,
        java.util.List.of(),
        false,
        0.0f,
        21,
        21,
        21,
        23,
        21,
        21,
        21,
        new int[] {90, 90, 90, 90, 60, 5000, 100, 100, 100, 100, 100, 100},
        7,
        43,
        0,
        1073741824,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d10+5", 94, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
