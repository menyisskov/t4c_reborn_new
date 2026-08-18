package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r215Skeleton extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r215Skeleton(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skeleton",
        "${monster.skeleton}",
        69,
        0,
        1,
        59,
        4,
        10,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
        7,
        22,
        java.util.List.of(
            new MonsterDef.LootDrop("Leather gloves", 0.03f),
            new MonsterDef.LootDrop("Ring of confidence", 0.01f),
            new MonsterDef.LootDrop("Healing potion", 0.02f),
            new MonsterDef.LootDrop("Light healing potion", 0.03f),
            new MonsterDef.LootDrop("Skeleton bone", 7.0E-4f)),
        false,
        0.0f,
        18,
        18,
        18,
        18,
        0,
        18,
        0,
        new int[] {92, 92, 122, 61, 5025, 61, 100, 100, 100, 100, 100, 100},
        4,
        26,
        0,
        1073741824,
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
        14,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d7+3", 58, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
