package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r184Pilferer extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public r184Pilferer(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Pilferer",
        "${monster.pilferer}",
        996,
        0,
        5,
        2708,
        50,
        113,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC#l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        80,
        247,
        java.util.List.of(
            new MonsterDef.LootDrop("Serious healing potion", 0.01f),
            new MonsterDef.LootDrop("Villain skull", 0.05f),
            new MonsterDef.LootDrop("Dagger of bleeding", 0.02f),
            new MonsterDef.LootDrop("Lost Psalms of Artherk p1", 0.01f),
            new MonsterDef.LootDrop("Lost Psalms of Artherk p2", 0.01f),
            new MonsterDef.LootDrop("Lost Psalms of Artherk p3", 0.01f),
            new MonsterDef.LootDrop("Lost Psalms of Artherk p4", 0.01f),
            new MonsterDef.LootDrop("Lost Psalms of Artherk p5", 0.01f),
            new MonsterDef.LootDrop("Rough moonstone", 0.001f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough agate", 0.02f),
            new MonsterDef.LootDrop("Hickory recurve bow", 0.005f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.0025f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.01f)),
        false,
        0.0f,
        60,
        55,
        55,
        69,
        0,
        55,
        0,
        new int[] {72, 72, 72, 72, 48, 5000, 100, 100, 100, 100, 100, 100},
        45,
        190,
        0,
        1077280768,
        20042,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        35,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d64+49", 550, 50, 0, 0, 10),
            new MonsterDef.Attack("", 0, 50, 10094, 0, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
