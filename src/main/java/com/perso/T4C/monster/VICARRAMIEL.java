package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "VICARRAMIEL", x = 1421, y = 2223, z = 1, stationary = false, aggressive = true)
public final class VICARRAMIEL extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public VICARRAMIEL(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "VICARRAMIEL",
        "${monster.vicarramiel}",
        1497,
        0,
        0,
        72997,
        88,
        201,
        30000L,
        "64kSkeletonKing#m",
        "64kSkeletonKingA#k",
        "64kSkeletonKingC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        107,
        330,
        java.util.List.of(),
        false,
        0.0f,
        75,
        68,
        68,
        87,
        100,
        68,
        34,
        new int[] {87, 87, 87, 87, 87, 5000, 150, 150, 150, 150, 150, 150},
        60,
        250,
        0,
        1106247680,
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
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d114+87", 1150, 33, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
