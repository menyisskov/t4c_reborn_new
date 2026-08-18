package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Defiler extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Defiler";

  public Defiler(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Defiler",
        "${monster.defiler}",
        334,
        0,
        2,
        496,
        16,
        37,
        30000L,
        "Scorpion#h",
        "ScorpionA#g",
        "ScorpionC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        34,
        104,
        java.util.List.of(),
        false,
        0.0f,
        34,
        32,
        32,
        37,
        0,
        32,
        0,
        new int[] {85, 85, 56, 113, 85, 5000, 100, 100, 100, 100, 100, 100},
        19,
        86,
        0,
        1075970048,
        20024,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d22+15", 238, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 25, 10747, 1, 10),
            new MonsterDef.Attack("", 0, 75, 10119, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
