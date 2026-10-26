package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
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
        2667,
        0,
        42,
        57052,
        131,
        267,
        30000L,
        "Rat#f",
        "RatA#i",
        "RatC!j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        158,
        484,
        java.util.List.of(
            new MonsterDef.LootDrop("potion_of_nimbleness", 0.02f),
            new MonsterDef.LootDrop("key_of_ascension", 0.005f),
            new MonsterDef.LootDrop("rough_malachite", 0.02f),
            new MonsterDef.LootDrop("rough_emerald", 0.02f)),
        false,
        0.0f,
        103,
        94,
        94,
        120,
        94,
        94,
        32,
        new int[] {42, 84, 42, 84, 62, 5000, 100, 100, 100, 100, 100, 100},
        88,
        362,
        0,
        1110441984,
        20003,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d151+118", 1066, 10, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
