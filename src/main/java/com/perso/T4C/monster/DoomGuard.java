package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DoomGuard extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Doom Guard";

  public DoomGuard(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Doom Guard",
        "${monster.doom_guard}",
        581,
        0,
        3,
        1154,
        28,
        63,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        53,
        165,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Potion of mana", 0.03f),
            new MonsterDef.LootDrop("Potion of fury", 0.01f),
            new MonsterDef.LootDrop("Skeleton bone", 0.001f),
            new MonsterDef.LootDrop("Pouch of Black Snakeroot", 0.02f),
            new MonsterDef.LootDrop("Skeleton bone", 0.008f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {79, 79, 106, 53, 5025, 53, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1076756480,
        20012,
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
            new MonsterDef.Attack("1d36+27", 370, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10086, 10, 13),
            new MonsterDef.Attack("", 0, 100, 10119, 5, 9)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
