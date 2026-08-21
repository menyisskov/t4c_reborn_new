package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class LesserDrake extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Lesser Drake";

  public LesserDrake(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Lesser Drake",
        "${monster.lesser_drake}",
        15385,
        0,
        0,
        0,
        1,
        10,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        449,
        1375,
        java.util.List.of(),
        false,
        0.0f,
        265,
        239,
        239,
        315,
        0,
        239,
        0,
        new int[] {63, 5000, 63, -63, 63, 5000, 100, 100, 100, 100, 100, 100},
        250,
        1010,
        0,
        1079984128,
        20036,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        1,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("if(1d10=1?target.hp/2:1d429+337)", 3010, 75, 0, 0, 1),
            new MonsterDef.Attack("", 0, 75, 10095, 2, 20),
            new MonsterDef.Attack("", 0, 25, 10616, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
