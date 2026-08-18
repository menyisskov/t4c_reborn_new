package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBSHRIEKERRODENT", x = 1816, y = 2865, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1853, y = 2884, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1855, y = 2901, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1861, y = 2853, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1889, y = 2858, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1894, y = 2955, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1904, y = 2934, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1914, y = 2965, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1927, y = 2704, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1934, y = 2712, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1935, y = 2935, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1988, y = 2704, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1999, y = 2886, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 1999, y = 2903, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 2019, y = 2900, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 2038, y = 2885, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 2043, y = 2880, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 2048, y = 2852, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 2059, y = 2865, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 2068, y = 2853, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 2090, y = 2860, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 2094, y = 2878, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 2101, y = 2851, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBSHRIEKERRODENT", x = 2104, y = 2869, z = 0, stationary = false, aggressive = true)
public final class ShriekerRodent extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Rat Attack.wav";
  public static final String SOUND_DEATH = "Rat Dying.wav";
  public static final String SOUND_HIT = "Rat Hit.wav";

  public ShriekerRodent(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBSHRIEKERRODENT",
        "${monster.mobshriekerrodent}",
        100,
        0,
        1,
        100,
        1,
        2,
        30000L,
        "",
        "",
        "",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        1,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        true,
        java.util.List.of(),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
