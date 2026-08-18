package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Olin Haad", x = 2211, y = 2811, z = 1, stationary = false, aggressive = false)
@Spawn(type = "OLINHAAD3", x = 1776, y = 2380, z = 1, stationary = false, aggressive = false)
public final class OLINHAAD3 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public OLINHAAD3(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "OLINHAAD3",
        "${monster.olinhaad3}",
        1000000,
        0,
        0,
        0,
        70,
        159,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        200,
        220,
        240,
        200,
        250,
        210,
        220,
        new int[] {5000, 5000, 5000, 5000, 5000, 5666, 150, 150, 150, 150, 150, 150},
        100,
        1,
        0,
        1176256512,
        10011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d90+69", 730, 25, 0, 0, 0)),
        false,
        0,
        java.util.List.of("Olin Haad"),
        java.util.Map.of());
  }
}
