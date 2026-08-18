package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Bone Demon", x = 127, y = 310, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 132, y = 375, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 150, y = 367, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 160, y = 335, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 165, y = 299, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 166, y = 163, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 166, y = 369, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 167, y = 320, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 182, y = 321, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 182, y = 356, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 184, y = 387, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 188, y = 330, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 192, y = 275, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 202, y = 144, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 208, y = 234, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 212, y = 189, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 218, y = 113, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 220, y = 301, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 230, y = 177, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 237, y = 272, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 240, y = 164, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 248, y = 245, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 249, y = 234, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 253, y = 151, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 257, y = 325, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 258, y = 93, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 263, y = 318, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 270, y = 139, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 277, y = 185, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 279, y = 328, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 282, y = 228, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 282, y = 98, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 285, y = 264, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 289, y = 271, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 298, y = 140, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 306, y = 159, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 316, y = 319, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 322, y = 198, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 324, y = 152, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 336, y = 310, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 345, y = 107, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 345, y = 194, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 348, y = 298, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 363, y = 114, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 381, y = 168, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 383, y = 148, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 384, y = 199, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 386, y = 135, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 412, y = 215, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Bone Demon", x = 415, y = 233, z = 0, stationary = false, aggressive = true)
public final class BoneDemon extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public static final String CANONICAL_NAME = "Bone Demon";

  public BoneDemon(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bone Demon",
        "${monster.bone_demon}",
        1571,
        0,
        6,
        5224,
        73,
        166,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        111,
        341,
        java.util.List.of(
            new MonsterDef.LootDrop("Mana elixir", 0.01f),
            new MonsterDef.LootDrop("Mithril blade 1", 0.01f),
            new MonsterDef.LootDrop("Scale mail", 0.005f),
            new MonsterDef.LootDrop("Scalemail boots", 0.01f),
            new MonsterDef.LootDrop("Scalemail gauntlets", 0.01f),
            new MonsterDef.LootDrop("Scalemail helmet", 0.01f),
            new MonsterDef.LootDrop("Scalemail leggings", 0.005f),
            new MonsterDef.LootDrop("Scalemail protector", 0.01f),
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f)),
        false,
        0.0f,
        77,
        70,
        70,
        89,
        0,
        70,
        0,
        new int[] {77, 77, 103, 51, 5025, 64, 100, 100, 100, 100, 100, 100},
        62,
        258,
        0,
        1077870592,
        20012,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        34,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d94+72", 754, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10375, 1, 10),
            new MonsterDef.Attack("", 0, 40, 10086, 1, 10),
            new MonsterDef.Attack("", 0, 40, 10091, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
