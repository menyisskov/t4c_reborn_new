package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Bat extends DataMonster {
  public static final String SOUND_ATTACK = "Bat Attack.wav";
  public static final String SOUND_DEATH = "Bat Dying.wav";
  public static final String SOUND_HIT = "Bat Hit.wav";

  public static final String CANONICAL_NAME = "Bat";

  public Bat(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bat",
        "${monster.bat}",
        27,
        0,
        1,
        21,
        2,
        5,
        30000L,
        "Bat#h",
        "BatA#i",
        "BatC#l",
        "Bat Attack.wav",
        "Bat Dying.wav",
        "Bat Hit.wav",
        1,
        5,
        java.util.List.of(
            new MonsterDef.LootDrop("Torch", 0.05f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f)),
        false,
        0.0f,
        12,
        15,
        18,
        16,
        0,
        14,
        0,
        new int[] {62, 124, 93, 93, 93, 5000, 100, 100, 100, 100, 100, 100},
        1,
        14,
        0,
        0,
        20002,
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
        java.util.List.of(new MonsterDef.Attack("1d4+1", 22, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
