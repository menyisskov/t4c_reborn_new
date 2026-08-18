package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r277WraithBat extends DataMonster {
  public static final String SOUND_ATTACK = "Bat Attack.wav";
  public static final String SOUND_DEATH = "Bat Dying.wav";
  public static final String SOUND_HIT = "Bat Hit.wav";

  public r277WraithBat(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Wraith Bat",
        "${monster.wraith_bat}",
        1684,
        0,
        6,
        5780,
        78,
        176,
        30000L,
        "Bat#h",
        "BatA#i",
        "BatC#l",
        "Bat Attack.wav",
        "Bat Dying.wav",
        "Bat Hit.wav",
        116,
        357,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough garnet", 0.001f),
            new MonsterDef.LootDrop("Rough ruby", 0.004f),
            new MonsterDef.LootDrop("Rough carnelian", 0.02f)),
        false,
        0.0f,
        80,
        73,
        73,
        93,
        0,
        73,
        0,
        new int[] {50, 101, 101, 50, 5025, 63, 100, 100, 100, 100, 100, 100},
        65,
        270,
        0,
        1077936128,
        20002,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        36,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d99+77", 790, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 80, 10119, 1, 10),
            new MonsterDef.Attack("", 0, 10, 10376, 1, 10),
            new MonsterDef.Attack("", 0, 10, 10381, 1, 10)),
        false,
        0,
        java.util.List.of("Dark Synk"),
        java.util.Map.of());
  }
}
