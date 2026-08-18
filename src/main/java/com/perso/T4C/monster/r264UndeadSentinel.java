package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r264UndeadSentinel extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r264UndeadSentinel(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Undead Sentinel",
        "${monster.undead_sentinel}",
        5641,
        0,
        10,
        30115,
        189,
        429,
        30000L,
        "64kSkeletonKing#m",
        "64kSkeletonKingA#k",
        "64kSkeletonKingC#p",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        251,
        770,
        java.util.List.of(),
        false,
        0.0f,
        155,
        140,
        140,
        183,
        0,
        140,
        0,
        new int[] {47, 47, 63, 31, 5025, 47, 100, 100, 100, 100, 100, 100},
        140,
        570,
        0,
        1079083008,
        20057,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        1,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d241+188", 1690, 50, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10090, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
