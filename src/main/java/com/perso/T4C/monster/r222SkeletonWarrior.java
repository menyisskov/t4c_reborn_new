package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r222SkeletonWarrior extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r222SkeletonWarrior(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skeleton Warrior",
        "${monster.skeleton_warrior}",
        254,
        0,
        2,
        337,
        13,
        29,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
        26,
        82,
        java.util.List.of(
            new MonsterDef.LootDrop("Rusted long sword", 0.01f),
            new MonsterDef.LootDrop("Leather belt", 0.05f),
            new MonsterDef.LootDrop("Healing potion", 0.03f),
            new MonsterDef.LootDrop("Skeleton bone", 9.0000004E-4f)),
        false,
        0.0f,
        30,
        35,
        28,
        34,
        0,
        28,
        0,
        new int[] {86, 86, 115, 57, 5025, 57, 100, 100, 100, 100, 100, 100},
        15,
        70,
        0,
        1075576832,
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
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d17+12", 190, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
