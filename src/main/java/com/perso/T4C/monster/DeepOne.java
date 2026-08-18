package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DeepOne extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Deep One";

  public DeepOne(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Deep One",
        "${monster.deep_one}",
        631,
        0,
        4,
        1311,
        30,
        69,
        30000L,
        "Atrocity#h",
        "AtrocityA#h",
        "AtrocityC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        57,
        176,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Potion of fury", 0.05f)),
        false,
        0.0f,
        47,
        43,
        43,
        53,
        0,
        43,
        0,
        new int[] {78, 78, 52, 105, 78, 5000, 100, 100, 100, 100, 100, 100},
        32,
        138,
        0,
        1076887552,
        20026,
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
        java.util.List.of(new MonsterDef.Attack("1d40+29", 394, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
