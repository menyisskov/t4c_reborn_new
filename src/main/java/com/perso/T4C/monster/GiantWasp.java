package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class GiantWasp extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public static final String CANONICAL_NAME = "Giant Wasp";

  public GiantWasp(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Giant Wasp",
        "${monster.giant_wasp}",
        199,
        0,
        2,
        236,
        10,
        23,
        30000L,
        "GiantWasp#h",
        "GiantWaspA#h",
        "GiantWaspC#k",
        "Wasp Attack.wav",
        "Wasp Dying.wav",
        "Wasp Hit.wav",
        21,
        66,
        java.util.List.of(new MonsterDef.LootDrop("Wasp wax", 0.01f)),
        false,
        0.0f,
        27,
        25,
        25,
        29,
        0,
        25,
        0,
        new int[] {58, 117, 88, 88, 88, 5000, 100, 100, 100, 100, 100, 100},
        12,
        88,
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
        16,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d14+9", 154, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10091, 1, 13)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
