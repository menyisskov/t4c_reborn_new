package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r195PutridBeast extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r195PutridBeast(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Putrid Beast",
        "${monster.putrid_beast}",
        2435,
        0,
        8,
        10129,
        112,
        254,
        30000L,
        "Taunting#h",
        "TauntingA#h",
        "TauntingC#m",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        149,
        456,
        java.util.List.of(
            new MonsterDef.LootDrop("Manastone", 0.01f),
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f)),
        false,
        0.0f,
        98,
        89,
        89,
        114,
        0,
        89,
        0,
        new int[] {43, 87, 65, 65, 65, 5000, 100, 100, 100, 100, 100, 100},
        83,
        342,
        0,
        1078231040,
        20038,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        37,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d143+111", 1006, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 5, 10350, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10378, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10359, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10119, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10120, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
