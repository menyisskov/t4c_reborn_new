package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r204RogueMage extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r204RogueMage(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Rogue Mage",
        "${monster.rogue_mage}",
        418,
        0,
        3,
        699,
        20,
        46,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        41,
        126,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Mana elixir", 0.03f),
            new MonsterDef.LootDrop("Ring of the bear", 0.01f),
            new MonsterDef.LootDrop("Green gem", 0.005f),
            new MonsterDef.LootDrop("Pouch of Woody Nightshade", 5.0E-4f),
            new MonsterDef.LootDrop("Pouch of Witch Hazel", 0.001f),
            new MonsterDef.LootDrop("Golden chalice", 5.0E-4f)),
        false,
        0.0f,
        38,
        35,
        35,
        42,
        0,
        35,
        0,
        new int[] {83, 83, 83, 83, 110, 5000, 100, 100, 100, 100, 100, 100},
        23,
        117,
        0,
        1075838976,
        10011,
        41148,
        40595,
        40020,
        0,
        40021,
        40655,
        0,
        0,
        50,
        23,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d27+19", 286, 100, 0, 0, 1),
            new MonsterDef.Attack("", 0, 70, 10096, 5, 12),
            new MonsterDef.Attack("", 0, 20, 10094, 5, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
