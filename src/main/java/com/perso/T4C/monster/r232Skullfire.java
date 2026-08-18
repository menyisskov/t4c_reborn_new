package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Skullfire", x = 553, y = 2117, z = 1, stationary = false, aggressive = true)
public final class r232Skullfire extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r232Skullfire(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skullfire",
        "${monster.skullfire}",
        1356,
        0,
        12,
        8424,
        64,
        106,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        200,
        616,
        java.util.List.of(
            new MonsterDef.LootDrop("Torch", 0.02f),
            new MonsterDef.LootDrop("Potion of mana", 0.05f),
            new MonsterDef.LootDrop("Mana elixir", 0.02f),
            new MonsterDef.LootDrop("Pouch of Witch Hazel", 0.008f),
            new MonsterDef.LootDrop("Skeleton bone", 0.0069999998f)),
        false,
        0.0f,
        71,
        65,
        65,
        82,
        0,
        65,
        0,
        new int[] {67, 67, 90, 45, 5025, 45, 100, 100, 100, 100, 100, 100},
        56,
        234,
        0,
        1077673984,
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
            new MonsterDef.Attack("1d43+63", 682, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 35, 10119, 6, 15),
            new MonsterDef.Attack("", 0, 55, 10096, 6, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
