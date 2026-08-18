package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Time Protector", x = 2664, y = 2192, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2672, y = 2166, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2672, y = 2220, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2698, y = 2141, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2739, y = 2316, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2754, y = 2148, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2781, y = 2355, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2799, y = 2375, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2848, y = 2201, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2854, y = 2322, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2872, y = 2222, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2889, y = 2241, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2911, y = 2247, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2911, y = 2268, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2917, y = 2280, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2919, y = 2257, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2919, y = 2258, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2924, y = 2270, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2925, y = 2298, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2935, y = 2247, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2936, y = 2260, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2937, y = 2285, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2939, y = 2272, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2949, y = 2265, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2951, y = 2255, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2959, y = 2275, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2967, y = 2257, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Time Protector", x = 2968, y = 2257, z = 2, stationary = false, aggressive = true)
public final class r245TimeProtector extends DataMonster {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public r245TimeProtector(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Time Protector",
        "${monster.time_protector}",
        6334,
        0,
        11,
        34954,
        0,
        0,
        30000L,
        "TreeEnt#i",
        "TreeEntA#i",
        "TreeEntC#j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        269,
        825,
        java.util.List.of(new MonsterDef.LootDrop("Bracer of leaves", 0.05f)),
        false,
        0.0f,
        165,
        149,
        149,
        195,
        0,
        149,
        0,
        new int[] {63, 63, 63, 63, 63, 5000, 100, 100, 100, 100, 100, 100},
        150,
        610,
        0,
        1079164928,
        20018,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        1,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("", 0, 100, 10120, 2, 15),
            new MonsterDef.Attack("1d258+152", 1810, 100, 10120, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
