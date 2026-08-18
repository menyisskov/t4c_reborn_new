package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r247TollTroll extends DataMonster {
  public static final String SOUND_ATTACK = "Troll Attack.wav";
  public static final String SOUND_DEATH = "Troll Dying.wav";
  public static final String SOUND_HIT = "Troll Hit.wav";

  public r247TollTroll(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Toll Troll",
        "${monster.toll_troll}",
        2527,
        0,
        8,
        10634,
        115,
        260,
        30000L,
        "GreenTroll#f",
        "GreenTrollA#g",
        "GreenTrollC#k",
        "Troll Attack.wav",
        "Troll Dying.wav",
        "Troll Hit.wav",
        152,
        467,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Gothic shield", 0.0025f),
            new MonsterDef.LootDrop("High metal bastard sword", 0.0025f)),
        false,
        0.0f,
        100,
        91,
        91,
        117,
        0,
        91,
        0,
        new int[] {64, 64, 64, 64, 64, 5000, 100, 100, 100, 100, 100, 100},
        85,
        350,
        0,
        1078263808,
        20010,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        38,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d146+114", 1030, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10314, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10094, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
