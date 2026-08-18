package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Decaying Zombie", x = 102, y = 603, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 131, y = 672, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 35, y = 447, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 38, y = 468, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 416, y = 235, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 428, y = 248, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 486, y = 290, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 49, y = 631, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 509, y = 264, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 527, y = 294, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 528, y = 267, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 534, y = 258, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 535, y = 238, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 548, y = 230, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 61, y = 644, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 64, y = 645, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 65, y = 645, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 67, y = 611, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 68, y = 611, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 69, y = 606, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 72, y = 600, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 78, y = 640, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 81, y = 650, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 82, y = 650, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 85, y = 606, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 88, y = 627, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Decaying Zombie", x = 97, y = 645, z = 1, stationary = false, aggressive = true)
public final class DecayingZombie extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public static final String CANONICAL_NAME = "Decaying Zombie";

  public DecayingZombie(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Decaying Zombie",
        "${monster.decaying_zombie}",
        55,
        0,
        1,
        46,
        4,
        8,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC#j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        5,
        16,
        java.util.List.of(
            new MonsterDef.LootDrop("Torch", 0.03f),
            new MonsterDef.LootDrop("Light healing potion", 0.05f)),
        false,
        0.0f,
        18,
        17,
        17,
        18,
        0,
        17,
        0,
        new int[] {92, 92, 123, 61, 5025, 61, 100, 100, 100, 100, 100, 100},
        3,
        22,
        0,
        1072693248,
        20009,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        14,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d5+3", 46, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
