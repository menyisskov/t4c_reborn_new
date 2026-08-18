package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Araf Kul", x = 2654, y = 565, z = 0, stationary = false, aggressive = true)
public final class ArafKul extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Araf Kul";

  public ArafKul(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Araf Kul",
        "${monster.araf_kul}",
        926,
        0,
        3,
        1620,
        22,
        50,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        88,
        274,
        java.util.List.of(
            new MonsterDef.LootDrop("Leaf of a pink tree", 0.1f),
            new MonsterDef.LootDrop("human bone", 0.07f),
            new MonsterDef.LootDrop("Pouch of yellow powder", 0.18f),
            new MonsterDef.LootDrop("Light healing potion", 0.1f)),
        false,
        0.0f,
        40,
        37,
        37,
        45,
        0,
        37,
        0,
        new int[] {109, 54, 82, 82, 82, 5000, 100, 100, 100, 100, 100, 100},
        25,
        110,
        0,
        1076363264,
        20008,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        15,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d29+21", 310, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10096, 2, 12),
            new MonsterDef.Attack("", 0, 100, 10088, 13, 20)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
