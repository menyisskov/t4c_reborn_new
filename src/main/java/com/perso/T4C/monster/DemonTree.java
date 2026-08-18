package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DemonTree extends DataMonster {
  public static final String SOUND_ATTACK = "Demon Attack.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

  public static final String CANONICAL_NAME = "Demon Tree";

  public DemonTree(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Demon Tree",
        "${monster.demon_tree}",
        396,
        0,
        3,
        641,
        19,
        43,
        30000L,
        "TreeEnt#i",
        "TreeEntA#i",
        "TreeEntC#j",
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
        39,
        121,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Healing potion", 0.03f),
            new MonsterDef.LootDrop("Red gem", 0.005f),
            new MonsterDef.LootDrop("Demon Tree Leaf", 0.05f),
            new MonsterDef.LootDrop("Demon tree wood", 0.05f)),
        false,
        0.0f,
        37,
        34,
        34,
        41,
        0,
        34,
        0,
        new int[] {111, 111, 111, 111, 111, 5000, 100, 100, 100, 100, 100, 100},
        22,
        98,
        0,
        1076232192,
        20018,
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
            new MonsterDef.Attack("1d25+18", 274, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 45, 10091, 8, 13),
            new MonsterDef.Attack("", 0, 45, 10120, 8, 13),
            new MonsterDef.Attack("", 0, 80, 10120, 3, 7)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
