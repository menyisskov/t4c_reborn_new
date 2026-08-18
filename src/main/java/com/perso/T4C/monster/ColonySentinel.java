package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class ColonySentinel extends DataMonster {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public static final String CANONICAL_NAME = "Colony Sentinel";

  public ColonySentinel(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Colony Sentinel",
        "${monster.colony_sentinel}",
        1058,
        0,
        5,
        3000,
        54,
        122,
        30000L,
        "GiantWasp#h",
        "GiantWaspA#h",
        "GiantWaspC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        84,
        258,
        java.util.List.of(
            new MonsterDef.LootDrop("Wasp wax", 0.005f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Finely cut aquamarine", 5.0E-4f),
            new MonsterDef.LootDrop("Finely cut sapphire", 0.002f),
            new MonsterDef.LootDrop("Finely cut amethyst", 0.01f)),
        false,
        0.0f,
        62,
        57,
        57,
        71,
        0,
        57,
        0,
        new int[] {47, 95, 71, 71, 71, 5000, 100, 100, 100, 100, 100, 100},
        47,
        198,
        0,
        1077346304,
        20029,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        32,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d69+53", 574, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 80, 10091, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10314, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
