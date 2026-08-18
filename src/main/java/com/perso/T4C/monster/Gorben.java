package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Gorben extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Gorben";

  public Gorben(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Gorben",
        "${monster.gorben}",
        1676,
        0,
        3,
        2926,
        50,
        77,
        30000L,
        "GoblinBoss#l",
        "GoblinBossA#i",
        "GoblinBossC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        106,
        330,
        java.util.List.of(
            new MonsterDef.LootDrop("Golden ring", 5.0E-4f),
            new MonsterDef.LootDrop("Light healing potion", 0.1f),
            new MonsterDef.LootDrop("Ringmail armor", 5.0E-4f),
            new MonsterDef.LootDrop("Ringmail leggings", 5.0E-4f),
            new MonsterDef.LootDrop("Ringmail helmet", 5.0E-4f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {106, 53, 79, 79, 79, 5025, 100, 100, 100, 100, 100, 100},
        30,
        100,
        0,
        1077018624,
        20041,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d28+49", 250, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10086, 5, 18)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
