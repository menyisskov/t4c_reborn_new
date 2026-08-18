package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class BoneDemon extends DataMonster {
  public static final String SOUND_ATTACK = "Demon Attack.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

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
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
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
