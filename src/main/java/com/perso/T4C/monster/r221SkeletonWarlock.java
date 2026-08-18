package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r221SkeletonWarlock extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r221SkeletonWarlock(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skeleton Warlock",
        "${monster.skeleton_warlock}",
        1027,
        0,
        5,
        2859,
        52,
        118,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
        82,
        253,
        java.util.List.of(
            new MonsterDef.LootDrop("Gleaming shard", 0.02f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Manastone", 0.02f),
            new MonsterDef.LootDrop("Potion of fury", 0.05f),
            new MonsterDef.LootDrop("Red cape", 0.01f),
            new MonsterDef.LootDrop("Stone key", 0.01f),
            new MonsterDef.LootDrop("Skeleton bone", 0.002f),
            new MonsterDef.LootDrop("Garb of the dead", 0.001f)),
        false,
        0.0f,
        61,
        56,
        56,
        70,
        0,
        56,
        0,
        new int[] {72, 72, 96, 48, 5025, 48, 100, 100, 100, 100, 100, 100},
        46,
        194,
        0,
        1077346304,
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
            new MonsterDef.Attack("1d67+51", 562, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10119, 7, 20),
            new MonsterDef.Attack("", 0, 50, 10090, 7, 20),
            new MonsterDef.Attack("", 0, 40, 10094, 3, 6),
            new MonsterDef.Attack("", 0, 55, 10120, 3, 6)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
