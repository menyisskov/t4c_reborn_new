package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Brigand extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Brigand";

  public Brigand(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Brigand",
        "${monster.brigand}",
        116,
        0,
        1,
        112,
        6,
        15,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        12,
        38,
        java.util.List.of(
            new MonsterDef.LootDrop("Leather gloves", 0.01f),
            new MonsterDef.LootDrop("Leather pants", 0.01f),
            new MonsterDef.LootDrop("Leather Helmet", 0.01f),
            new MonsterDef.LootDrop("Light healing potion", 0.15f),
            new MonsterDef.LootDrop("Iron key", 0.01f)),
        false,
        0.0f,
        21,
        21,
        21,
        23,
        0,
        21,
        0,
        new int[] {90, 90, 90, 90, 60, 5000, 100, 100, 100, 100, 100, 100},
        7,
        43,
        0,
        1073741824,
        20006,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        40,
        11,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d10+5", 94, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
