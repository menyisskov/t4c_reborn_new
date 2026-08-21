package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBMERCENARYC", x = 2795, y = 972, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYC", x = 2796, y = 986, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYC", x = 2800, y = 978, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYC", x = 2808, y = 970, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYC", x = 2809, y = 978, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYC", x = 2815, y = 964, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYC", x = 2821, y = 965, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMERCENARYC", x = 2831, y = 965, z = 0, stationary = false, aggressive = true)
public final class r280MOBMERCENARYC extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public r280MOBMERCENARYC(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBMERCENARYC",
        "${monster.mobmercenaryc}",
        116,
        0,
        9,
        563,
        6,
        15,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC!l",
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
        10004,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d10+5", 94, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
