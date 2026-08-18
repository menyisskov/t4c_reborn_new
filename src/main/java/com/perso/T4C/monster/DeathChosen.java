package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DeathChosen extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Death Chosen";

  public DeathChosen(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Death Chosen",
        "${monster.death_chosen}",
        847,
        0,
        4,
        2090,
        42,
        94,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        71,
        220,
        java.util.List.of(
            new MonsterDef.LootDrop("Gleaming shard", 0.02f),
            new MonsterDef.LootDrop("Potion of fury", 0.05f),
            new MonsterDef.LootDrop("Red cape", 0.01f),
            new MonsterDef.LootDrop("Defender", 0.01f),
            new MonsterDef.LootDrop("Stone key", 0.01f),
            new MonsterDef.LootDrop("Skeleton bone", 0.001f),
            new MonsterDef.LootDrop("Scalemail leggings", 2.0E-4f),
            new MonsterDef.LootDrop("Scalemail gauntlets", 2.0E-4f)),
        false,
        0.0f,
        55,
        50,
        50,
        63,
        0,
        50,
        0,
        new int[] {75, 75, 100, 50, 5025, 50, 100, 100, 100, 100, 100, 100},
        40,
        170,
        0,
        1077149696,
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
            new MonsterDef.Attack("1d53+41", 490, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 80, 10119, 4, 20)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
