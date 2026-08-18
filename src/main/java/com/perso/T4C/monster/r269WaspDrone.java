package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r269WaspDrone extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public r269WaspDrone(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Wasp Drone",
        "${monster.wasp_drone}",
        847,
        0,
        4,
        2090,
        42,
        94,
        30000L,
        "GiantWasp#h",
        "GiantWaspA#h",
        "GiantWaspC#k",
        "Wasp Attack.wav",
        "Wasp Dying.wav",
        "Wasp Hit.wav",
        71,
        220,
        java.util.List.of(
            new MonsterDef.LootDrop("Wasp wax", 0.005f),
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Finely cut malachite", 0.01f),
            new MonsterDef.LootDrop("Finely cut emerald", 0.002f),
            new MonsterDef.LootDrop("Finely cut limestone", 5.0E-4f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f)),
        false,
        0.0f,
        55,
        50,
        50,
        63,
        0,
        50,
        0,
        new int[] {50, 100, 75, 75, 75, 5000, 100, 100, 100, 100, 100, 100},
        40,
        170,
        0,
        1077149696,
        20029,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        32,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d53+41", 490, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10091, 2, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
