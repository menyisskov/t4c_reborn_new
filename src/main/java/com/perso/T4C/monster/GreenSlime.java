package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Green Slime", x = 126, y = 261, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 149, y = 451, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 172, y = 537, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 181, y = 448, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 182, y = 315, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 189, y = 328, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 191, y = 360, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 196, y = 471, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 198, y = 296, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 210, y = 446, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 210, y = 509, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 221, y = 492, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 228, y = 420, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 235, y = 483, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 241, y = 537, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 255, y = 483, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 257, y = 536, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 269, y = 475, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 277, y = 466, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 400, y = 402, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Green Slime", x = 410, y = 408, z = 1, stationary = false, aggressive = true)
public final class GreenSlime extends DataMonster {
  public static final String SOUND_ATTACK = "Ooze Attack.wav";
  public static final String SOUND_DEATH = "Ooze Dying.wav";
  public static final String SOUND_HIT = "Ooze Hit.wav";

  public static final String CANONICAL_NAME = "Green Slime";

  public GreenSlime(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Green Slime",
        "${monster.green_slime}",
        41,
        0,
        1,
        34,
        3,
        7,
        30000L,
        "Slime@023",
        "Slimea@35",
        "SlimeC!z",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        3,
        11,
        java.util.List.of(),
        false,
        0.0f,
        15,
        10,
        25,
        17,
        0,
        16,
        0,
        new int[] {61, 123, 123, 61, 93, 5000, 100, 100, 100, 100, 100, 100},
        2,
        8,
        0,
        1074266112,
        20005,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        60,
        16,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d5+2", 34, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
