package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r154Mysticist extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r154Mysticist(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Mysticist",
        "${monster.mysticist}",
        3011,
        0,
        3,
        5950,
        30,
        90,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        134,
        412,
        java.util.List.of(
            new MonsterDef.LootDrop("Escape scroll", 0.008f),
            new MonsterDef.LootDrop("Drachenshield", 0.004f),
            new MonsterDef.LootDrop("Cloak of the Skull", 0.02f),
            new MonsterDef.LootDrop("Manastone", 0.01f),
            new MonsterDef.LootDrop("Scroll of detect hidden", 0.01f),
            new MonsterDef.LootDrop("Scroll of detect invisible", 0.01f),
            new MonsterDef.LootDrop("Scroll of earthen strength", 0.01f),
            new MonsterDef.LootDrop("Scroll of orientation middle", 0.03f)),
        false,
        0.0f,
        90,
        82,
        82,
        105,
        0,
        82,
        0,
        new int[] {58, 58, 58, 58, 78, 5000, 100, 100, 100, 100, 100, 100},
        75,
        500,
        0,
        1077805056,
        10011,
        41148,
        0,
        0,
        0,
        0,
        40672,
        0,
        0,
        100,
        35,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d61+29", 750, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 10, 10652, 2, 15),
            new MonsterDef.Attack("", 0, 90, 10094, 2, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
