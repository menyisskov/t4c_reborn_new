package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r218SkeletonServant1 extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r218SkeletonServant1(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skeleton Servant 1",
        "${monster.skeleton_servant_1}",
        10000,
        0,
        0,
        0,
        1,
        10,
        30000L,
        "64kSkeletonServant1#m",
        "64kSkeletonServant1A#m",
        "64kSkeletonServant1C#p",
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        100,
        100,
        100,
        100,
        0,
        100,
        0,
        new int[] {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
        100,
        100,
        0,
        1079574528,
        20055,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        21,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d10", 50, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
