package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DeathStinger extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Death Stinger";

  public DeathStinger(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Death Stinger",
        "${monster.death_stinger}",
        657,
        0,
        4,
        1393,
        31,
        71,
        30000L,
        "GiantWasp#h",
        "GiantWaspA#h",
        "GiantWaspC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        59,
        181,
        java.util.List.of(new MonsterDef.LootDrop("Wasp wax", 0.2f)),
        false,
        0.0f,
        48,
        44,
        44,
        54,
        0,
        44,
        0,
        new int[] {52, 104, 78, 78, 78, 5000, 100, 100, 100, 100, 100, 100},
        33,
        222,
        0,
        0,
        20029,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d41+30", 406, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10091, 3, 13)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
