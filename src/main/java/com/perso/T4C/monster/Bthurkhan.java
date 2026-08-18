package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Bthurkhan extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Bthurkhan";

  public Bthurkhan(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bthurkhan",
        "${monster.bthurkhan}",
        4677,
        0,
        10,
        23665,
        0,
        0,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        224,
        687,
        java.util.List.of(new MonsterDef.LootDrop("Solid gold key", 0.15f)),
        false,
        0.0f,
        140,
        127,
        127,
        165,
        0,
        127,
        0,
        new int[] {47, 63, 47, 63, 63, 5000, 100, 100, 100, 100, 100, 100},
        125,
        510,
        0,
        1078919168,
        20036,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        35,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("", 0, 50, 10095, 2, 20),
            new MonsterDef.Attack("1d215+123", 1510, 100, 0, 0, 1),
            new MonsterDef.Attack("", 0, 40, 10088, 2, 20),
            new MonsterDef.Attack("", 0, 10, 10382, 2, 20)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
