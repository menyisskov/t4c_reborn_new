package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r274Wolfhound extends DataMonster {
  public static final String SOUND_ATTACK = "Wolf Attack.wav";
  public static final String SOUND_DEATH = "Wolf Dying.wav";
  public static final String SOUND_HIT = "Wolf Hit.wav";

  public r274Wolfhound(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Wolfhound",
        "${monster.wolfhound}",
        1090,
        0,
        5,
        3148,
        56,
        126,
        30000L,
        "Wolf#i",
        "WolfA#i",
        "WolfC#n",
        "Wolf Attack.wav",
        "Wolf Dying.wav",
        "Wolf Hit.wav",
        86,
        264,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f)),
        false,
        0.0f,
        63,
        58,
        58,
        72,
        0,
        58,
        0,
        new int[] {71, 71, 71, 71, 47, 5000, 100, 100, 100, 100, 100, 100},
        48,
        202,
        0,
        1077411840,
        20045,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        33,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d71+55", 586, 95, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
