package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Bonedead extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public static final String CANONICAL_NAME = "Bonedead";

  public Bonedead(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bonedead",
        "${monster.bonedead}",
        626,
        0,
        2,
        906,
        15,
        35,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        64,
        198,
        java.util.List.of(
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Mana elixir", 0.05f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Potion of fury", 0.1f),
            new MonsterDef.LootDrop("Amulet of dodging", 0.01f)),
        false,
        0.0f,
        33,
        31,
        31,
        36,
        0,
        31,
        0,
        new int[] {85, 85, 113, 56, 5025, 56, 100, 100, 100, 100, 100, 100},
        18,
        82,
        0,
        1075970048,
        20012,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        70,
        21,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d21+14", 226, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10086, 3, 12),
            new MonsterDef.Attack("", 0, 40, 10119, 3, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
