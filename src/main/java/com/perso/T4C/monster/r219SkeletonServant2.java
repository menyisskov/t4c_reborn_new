package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r219SkeletonServant2 extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r219SkeletonServant2(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skeleton Servant 2",
        "${monster.skeleton_servant_2}",
        10000,
        0,
        0,
        0,
        1,
        10,
        30000L,
        "64kSkeletonServant2#m",
        "64kSkeletonServant2A#k",
        "64kSkeletonServant2C#p",
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
        20056,
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
