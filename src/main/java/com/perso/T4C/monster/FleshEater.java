package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class FleshEater extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Flesh Eater";

  public FleshEater(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Flesh Eater",
        "${monster.flesh_eater}",
        558,
        0,
        3,
        1072,
        26,
        60,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        52,
        159,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Healing potion", 0.03f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Mummy bandages", 0.02f)),
        false,
        0.0f,
        44,
        41,
        41,
        49,
        0,
        41,
        0,
        new int[] {80, 80, 106, 53, 5025, 53, 100, 100, 100, 100, 100, 100},
        29,
        126,
        0,
        1076625408,
        20011,
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
            new MonsterDef.Attack("1d35+25", 358, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 80, 10119, 3, 13)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
