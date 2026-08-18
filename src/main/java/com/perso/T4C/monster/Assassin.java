package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Assassin extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Assassin";

  public Assassin(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Assassin",
        "${monster.assassin}",
        235,
        0,
        2,
        302,
        12,
        27,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        25,
        77,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.05f),
            new MonsterDef.LootDrop("Golden ring", 5.0E-4f)),
        false,
        0.0f,
        29,
        27,
        27,
        31,
        0,
        27,
        0,
        new int[] {87, 87, 87, 87, 58, 5000, 100, 100, 100, 100, 100, 100},
        14,
        66,
        0,
        1075052544,
        20042,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        11,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d16+11", 188, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
