package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class r242TestSkeletonCentaur extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r242TestSkeletonCentaur(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Test Skeleton Centaur",
        "${monster.test_skeleton_centaur}",
        500,
        0,
        1,
        1,
        11,
        20,
        30000L,
        "64kCentaurSkeleton#i",
        "64kCentaurSkeletonA#i",
        "64kCentaurSkeletonC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        1,
        1,
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
        1000,
        10,
        0,
        1076101120,
        20063,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        13,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d10+10", 300, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
