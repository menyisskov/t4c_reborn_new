package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class CaveDweller extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Cave Dweller";

  public CaveDweller(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Cave Dweller",
        "${monster.cave_dweller}",
        1219,
        0,
        5,
        3614,
        59,
        133,
        30000L,
        "Atrocity#h",
        "AtrocityA#h",
        "AtrocityC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        93,
        286,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Manastone", 0.03f),
            new MonsterDef.LootDrop("Serious healing potion", 0.03f),
            new MonsterDef.LootDrop("Healing potion", 0.03f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.01f)),
        false,
        0.0f,
        67,
        61,
        61,
        77,
        0,
        61,
        0,
        new int[] {69, 69, 46, 92, 69, 5000, 100, 100, 100, 100, 100, 100},
        52,
        218,
        0,
        1077542912,
        20026,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        90,
        35,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d75+58", 634, 45, 10096, 0, 10),
            new MonsterDef.Attack("", 0, 15, 10348, 0, 10),
            new MonsterDef.Attack("", 0, 5, 10321, 0, 10),
            new MonsterDef.Attack("", 0, 25, 10094, 0, 10),
            new MonsterDef.Attack("", 0, 10, 10364, 0, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
