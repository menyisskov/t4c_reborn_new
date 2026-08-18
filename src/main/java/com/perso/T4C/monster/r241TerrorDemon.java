package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r241TerrorDemon extends DataMonster {
  public static final String SOUND_ATTACK = "Demon Attack.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

  public r241TerrorDemon(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Terror Demon",
        "${monster.terror_demon}",
        1497,
        0,
        6,
        4869,
        70,
        159,
        30000L,
        "Demon#i",
        "DemonA#i",
        "DemonC#k",
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
        107,
        330,
        java.util.List.of(),
        false,
        0.0f,
        75,
        68,
        68,
        87,
        0,
        68,
        0,
        new int[] {87, 87, 87, 87, 87, 5000, 100, 100, 100, 100, 100, 100},
        60,
        250,
        0,
        1077805056,
        20013,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d90+69", 730, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
