package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class GangreneCarrier extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Gangrene Carrier";

  public GangreneCarrier(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Gangrene Carrier",
        "${monster.gangrene_carrier}",
        2301,
        0,
        8,
        9409,
        108,
        245,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC#j",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        143,
        440,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough agate", 0.02f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough moonstone", 0.001f),
            new MonsterDef.LootDrop("Garb of the dead", 0.005f)),
        false,
        0.0f,
        95,
        86,
        86,
        111,
        0,
        86,
        0,
        new int[] {67, 67, 90, 45, 5025, 56, 100, 100, 100, 100, 100, 100},
        80,
        330,
        0,
        1078198272,
        20009,
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
            new MonsterDef.Attack("1d138+107", 970, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10378, 1, 10),
            new MonsterDef.Attack("", 0, 5, 10379, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10376, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10119, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10090, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10358, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
