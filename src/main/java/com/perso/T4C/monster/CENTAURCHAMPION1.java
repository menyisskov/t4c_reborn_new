package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class CENTAURCHAMPION1 extends DataMonster {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public static final String CANONICAL_NAME = "CENTAURCHAMPION1";

  public CENTAURCHAMPION1(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "CENTAURCHAMPION1",
        "${monster.centaurchampion1}",
        2086,
        0,
        37,
        39279,
        93,
        211,
        30000L,
        "64kCentaurWarrior#i",
        "64kCentaurWarriorA#i",
        "64kCentaurWarriorC#n",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        134,
        412,
        java.util.List.of(),
        false,
        0.0f,
        90,
        82,
        82,
        105,
        82,
        82,
        30,
        new int[] {116, 116, 116, 116, 78, 5001, 100, 100, 100, 100, 100, 100},
        75,
        310,
        0,
        1108606976,
        20051,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d119+92", 910, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
