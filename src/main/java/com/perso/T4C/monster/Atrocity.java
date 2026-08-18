package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Atrocity extends DataMonster {
  public static final String SOUND_ATTACK = "Atrocity Attack.wav";
  public static final String SOUND_DEATH = "Atrocity Dying.wav";
  public static final String SOUND_HIT = "Atrocity Hit.wav";

  public static final String CANONICAL_NAME = "Atrocity";

  public Atrocity(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Atrocity",
        "${monster.atrocity}",
        84,
        0,
        1,
        77,
        5,
        12,
        30000L,
        "Atrocity#h",
        "AtrocityA#h",
        "AtrocityC#k",
        "Atrocity Attack.wav",
        "Atrocity Dying.wav",
        "Atrocity Hit.wav",
        8,
        27,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.03f),
            new MonsterDef.LootDrop("Light healing potion", 0.05f),
            new MonsterDef.LootDrop("Iron key", 0.005f)),
        false,
        0.0f,
        20,
        20,
        20,
        20,
        0,
        18,
        0,
        new int[] {91, 91, 60, 121, 91, 5000, 100, 100, 100, 100, 100, 100},
        5,
        30,
        0,
        1073741824,
        20026,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        55,
        10,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d8+4", 70, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
