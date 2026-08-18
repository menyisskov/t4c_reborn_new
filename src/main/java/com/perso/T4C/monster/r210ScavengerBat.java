package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Scavenger Bat", x = 463, y = 241, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Scavenger Bat", x = 472, y = 303, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Scavenger Bat", x = 495, y = 261, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Scavenger Bat", x = 496, y = 208, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Scavenger Bat", x = 497, y = 301, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Scavenger Bat", x = 511, y = 308, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Scavenger Bat", x = 519, y = 237, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Scavenger Bat", x = 548, y = 216, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Scavenger Bat", x = 558, y = 238, z = 1, stationary = false, aggressive = true)
public final class r210ScavengerBat extends DataMonster {
  public static final String SOUND_ATTACK = "Bat Attack.wav";
  public static final String SOUND_DEATH = "Bat Dying.wav";
  public static final String SOUND_HIT = "Bat Hit.wav";

  public r210ScavengerBat(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Scavenger Bat",
        "${monster.scavenger_bat}",
        181,
        0,
        2,
        209,
        9,
        22,
        30000L,
        "Bat#h",
        "BatA#i",
        "BatC#l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        19,
        60,
        java.util.List.of(),
        false,
        0.0f,
        25,
        24,
        24,
        28,
        0,
        24,
        0,
        new int[] {59, 118, 88, 88, 88, 5000, 100, 100, 100, 100, 100, 100},
        11,
        79,
        0,
        0,
        20002,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        21,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d14+8", 142, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
