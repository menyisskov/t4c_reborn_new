package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r220SkeletonWarder extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r220SkeletonWarder(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skeleton Warder",
        "${monster.skeleton_warder}",
        2086,
        0,
        7,
        7855,
        93,
        211,
        30000L,
        "64kSkeletonServant1#m",
        "64kSkeletonServant1A#m",
        "64kSkeletonServant1C#p",
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
        134,
        412,
        java.util.List.of(new MonsterDef.LootDrop("Warders cloak", 0.02f)),
        false,
        0.0f,
        90,
        82,
        82,
        105,
        0,
        82,
        0,
        new int[] {58, 58, 78, 39, 5025, 39, 100, 100, 100, 100, 100, 100},
        75,
        310,
        0,
        1078099968,
        20055,
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
        java.util.List.of(new MonsterDef.Attack("1d119+92", 910, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
