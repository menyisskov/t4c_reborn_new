package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Wraith Bat", x = 393, y = 510, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 407, y = 550, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 417, y = 485, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 455, y = 453, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 457, y = 604, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 477, y = 625, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 478, y = 422, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 520, y = 623, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 521, y = 427, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 543, y = 598, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 565, y = 471, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 570, y = 458, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 574, y = 577, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 582, y = 438, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 590, y = 560, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 600, y = 430, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 603, y = 510, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 605, y = 536, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 614, y = 422, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 619, y = 380, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 629, y = 402, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 635, y = 358, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 637, y = 350, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 646, y = 337, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 669, y = 393, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 674, y = 402, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Wraith Bat", x = 679, y = 381, z = 1, stationary = false, aggressive = true)
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
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
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
