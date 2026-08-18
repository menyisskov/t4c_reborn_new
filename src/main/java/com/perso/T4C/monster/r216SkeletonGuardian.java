package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r216SkeletonGuardian extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r216SkeletonGuardian(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skeleton Guardian",
        "${monster.skeleton_guardian}",
        217,
        0,
        2,
        267,
        11,
        25,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
        23,
        71,
        java.util.List.of(
            new MonsterDef.LootDrop("Leather belt", 0.02f),
            new MonsterDef.LootDrop("Iron key", 0.0069999998f),
            new MonsterDef.LootDrop("Healing potion", 0.02f),
            new MonsterDef.LootDrop("Skeleton bone", 8.0E-4f)),
        false,
        0.0f,
        27,
        26,
        26,
        30,
        0,
        26,
        0,
        new int[] {87, 87, 116, 58, 5025, 58, 100, 100, 100, 100, 100, 100},
        13,
        62,
        0,
        1075314688,
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
        21,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d15+10", 166, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10086, 5, 13)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
