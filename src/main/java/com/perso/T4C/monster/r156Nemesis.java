package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r156Nemesis extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r156Nemesis(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Nemesis",
        "${monster.nemesis}",
        2301,
        0,
        8,
        9409,
        108,
        245,
        30000L,
        "Scorpion#h",
        "ScorpionA#g",
        "ScorpionC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        143,
        440,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough moonstone", 0.001f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough agate", 0.02f)),
        false,
        0.0f,
        95,
        86,
        86,
        111,
        0,
        86,
        0,
        new int[] {67, 67, 45, 90, 67, 5000, 100, 100, 100, 100, 100, 100},
        80,
        330,
        0,
        1078198272,
        20024,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        40,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d138+107", 970, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10317, 3, 10),
            new MonsterDef.Attack("", 0, 10, 10321, 3, 10),
            new MonsterDef.Attack("", 0, 70, 10091, 3, 10),
            new MonsterDef.Attack("", 0, 10, 10346, 3, 10),
            new MonsterDef.Attack("", 0, 3, 10344, 0, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
