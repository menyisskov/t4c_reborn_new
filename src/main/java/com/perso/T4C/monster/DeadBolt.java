package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Dead Bolt", x = 851, y = 1707, z = 2, stationary = false, aggressive = true)
public final class DeadBolt extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Dead Bolt";

  public DeadBolt(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Dead Bolt",
        "${monster.dead_bolt}",
        2642,
        0,
        6,
        8125,
        63,
        143,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        196,
        604,
        java.util.List.of(
            new MonsterDef.LootDrop("Potion of fury", 0.05f),
            new MonsterDef.LootDrop("Plate boots", 0.001f),
            new MonsterDef.LootDrop("Skeleton bone", 0.01f),
            new MonsterDef.LootDrop("Pouch of Witch Hazel", 0.01f)),
        false,
        0.0f,
        70,
        64,
        64,
        81,
        0,
        64,
        0,
        new int[] {68, 68, 90, 45, 5025, 45, 100, 100, 100, 100, 100, 100},
        55,
        230,
        0,
        1077608448,
        20012,
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
        java.util.List.of(
            new MonsterDef.Attack("1d81+62", 670, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 75, 10120, 7, 12),
            new MonsterDef.Attack("", 0, 15, 10119, 7, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
