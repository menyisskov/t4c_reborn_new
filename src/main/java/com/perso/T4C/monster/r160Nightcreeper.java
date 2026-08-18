package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r160Nightcreeper extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r160Nightcreeper(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Nightcreeper",
        "${monster.nightcreeper}",
        847,
        0,
        4,
        2090,
        42,
        94,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        71,
        220,
        java.util.List.of(
            new MonsterDef.LootDrop("Ring of the rogue", 0.03f),
            new MonsterDef.LootDrop("Serious healing potion", 0.03f),
            new MonsterDef.LootDrop("Elven leather armor", 0.001f),
            new MonsterDef.LootDrop("Elven leather belt", 0.001f),
            new MonsterDef.LootDrop("Elven leather boots", 0.001f),
            new MonsterDef.LootDrop("Elven leather gloves", 0.001f),
            new MonsterDef.LootDrop("Elven leather helmet", 0.001f),
            new MonsterDef.LootDrop("Elven leather leggings", 0.001f),
            new MonsterDef.LootDrop("Dagger of bleeding", 0.02f),
            new MonsterDef.LootDrop("Villain skull", 0.04f),
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Hickory recurve bow", 0.005f),
            new MonsterDef.LootDrop("Cord of treachery", 0.003f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.01f)),
        false,
        0.0f,
        55,
        50,
        50,
        63,
        0,
        50,
        0,
        new int[] {75, 75, 75, 75, 50, 5000, 100, 100, 100, 100, 100, 100},
        40,
        170,
        0,
        1077149696,
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
            new MonsterDef.Attack("1d53+41", 490, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 10, 10357, 1, 12),
            new MonsterDef.Attack("", 0, 5, 10347, 1, 12),
            new MonsterDef.Attack("", 0, 85, 10094, 1, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
