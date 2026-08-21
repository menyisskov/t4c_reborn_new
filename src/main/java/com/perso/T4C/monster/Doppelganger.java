package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "DOPPELGANGER", x = 2790, y = 1030, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2790, y = 1090, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2790, y = 1150, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2790, y = 1210, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2790, y = 1270, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2850, y = 1030, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2850, y = 1090, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2850, y = 1150, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2850, y = 1210, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2850, y = 1270, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2910, y = 1030, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2910, y = 1090, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2910, y = 1150, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2910, y = 1210, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2910, y = 1270, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2970, y = 1030, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2970, y = 1090, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2970, y = 1150, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2970, y = 1210, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 2970, y = 1270, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 3030, y = 1030, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 3030, y = 1090, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 3030, y = 1150, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 3030, y = 1210, z = 1, stationary = false, aggressive = true)
@Spawn(type = "DOPPELGANGER", x = 3030, y = 1270, z = 1, stationary = false, aggressive = true)
public final class Doppelganger extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public Doppelganger(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "DOPPELGANGER",
        "${monster.doppelganger}",
        50000,
        0,
        0,
        0,
        1,
        5,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        115,
        104,
        104,
        135,
        104,
        104,
        35,
        new int[] {100, 100, 100, 100, 100, 5000, 100, 100, 100, 100, 100, 100},
        100,
        0,
        0,
        0,
        10011,
        278,
        288,
        0,
        0,
        0,
        277,
        0,
        472,
        100,
        0,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack(
                "target.maxhp/(5*(target.viewflag(30419))+1d5)", 65535, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
