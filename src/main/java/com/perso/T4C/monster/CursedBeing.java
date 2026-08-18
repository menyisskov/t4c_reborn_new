package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class CursedBeing extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Cursed Being";

  public CursedBeing(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Cursed Being",
        "${monster.cursed_being}",
        199,
        0,
        2,
        235,
        10,
        23,
        30000L,
        "Atrocity#h",
        "AtrocityA#h",
        "AtrocityC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        21,
        66,
        java.util.List.of(
            new MonsterDef.LootDrop("Diamond necklace", 0.003f),
            new MonsterDef.LootDrop("Leather belt", 0.04f),
            new MonsterDef.LootDrop("Iron ring", 0.05f)),
        false,
        0.0f,
        30,
        25,
        26,
        20,
        0,
        25,
        0,
        new int[] {88, 88, 58, 117, 88, 5000, 100, 100, 100, 100, 100, 100},
        12,
        58,
        0,
        1075314688,
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
        21,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d14+9", 154, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
