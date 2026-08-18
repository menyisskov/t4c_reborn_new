package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r265UnseenBat extends DataMonster {
  public static final String SOUND_ATTACK = "Bat Attack.wav";
  public static final String SOUND_DEATH = "Bat Dying.wav";
  public static final String SOUND_HIT = "Bat Hit.wav";

  public r265UnseenBat(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Unseen Bat",
        "${monster.unseen_bat}",
        1153,
        0,
        6,
        3462,
        60,
        135,
        30000L,
        "",
        null,
        null,
        "Bat Attack.wav",
        "Bat Dying.wav",
        "Bat Hit.wav",
        89,
        275,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f)),
        false,
        0.0f,
        65,
        59,
        59,
        75,
        0,
        59,
        0,
        new int[] {46, 93, 46, 93, 70, 5000, 100, 100, 100, 100, 100, 100},
        50,
        210,
        0,
        1077477376,
        21002,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        75,
        35,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d76+59", 620, 45, 0, 0, 10),
            new MonsterDef.Attack("", 0, 5, 10376, 0, 10),
            new MonsterDef.Attack("", 0, 25, 10096, 0, 10),
            new MonsterDef.Attack("", 0, 5, 10366, 0, 10),
            new MonsterDef.Attack("", 0, 2, 10382, 0, 10),
            new MonsterDef.Attack("", 0, 3, 10357, 0, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
