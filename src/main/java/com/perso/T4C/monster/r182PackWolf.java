package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r182PackWolf extends DataMonster {
  public static final String SOUND_ATTACK = "Wolf Attack.wav";
  public static final String SOUND_DEATH = "Wolf Dying.wav";
  public static final String SOUND_HIT = "Wolf Hit.wav";

  public r182PackWolf(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Pack Wolf",
        "${monster.pack_wolf}",
        847,
        0,
        4,
        2090,
        42,
        94,
        30000L,
        "Wolf#i",
        "WolfA#i",
        "WolfC#n",
        "Wolf Attack.wav",
        "Wolf Dying.wav",
        "Wolf Hit.wav",
        71,
        220,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Finely cut aquamarine", 5.0E-4f),
            new MonsterDef.LootDrop("Finely cut sapphire", 0.002f),
            new MonsterDef.LootDrop("Finely cut amethyst", 0.01f)),
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
        20045,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        75,
        33,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d53+41", 490, 97, 0, 0, 0)),
        true,
        40,
        java.util.List.of(),
        java.util.Map.of());
  }
}
