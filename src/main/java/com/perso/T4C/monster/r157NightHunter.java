package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r157NightHunter extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r157NightHunter(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Night Hunter",
        "${monster.night_hunter}",
        1153,
        0,
        6,
        3462,
        60,
        135,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        89,
        275,
        java.util.List.of(
            new MonsterDef.LootDrop("Blackened iron key", 0.05f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Torch", 0.05f),
            new MonsterDef.LootDrop("Light healing potion", 0.05f),
            new MonsterDef.LootDrop("Potion of cure rabies", 0.05f),
            new MonsterDef.LootDrop("Potion of fury", 0.01f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.01f)),
        false,
        0.0f,
        65,
        59,
        59,
        75,
        0,
        59,
        0,
        new int[] {70, 70, 70, 70, 92, 5000, 100, 100, 100, 100, 100, 100},
        50,
        210,
        0,
        1077477376,
        20042,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        75,
        34,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d76+59", 610, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 25, 10119, 1, 11),
            new MonsterDef.Attack("", 0, 25, 10384, 1, 11),
            new MonsterDef.Attack("", 0, 10, 10391, 1, 11)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
