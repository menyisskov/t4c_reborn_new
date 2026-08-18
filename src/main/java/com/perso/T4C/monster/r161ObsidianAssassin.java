package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r161ObsidianAssassin extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r161ObsidianAssassin(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Obsidian Assassin",
        "${monster.obsidian_assassin}",
        2761,
        0,
        8,
        11947,
        121,
        276,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        161,
        495,
        java.util.List.of(
            new MonsterDef.LootDrop("Key of the Lost Soul", 0.03f),
            new MonsterDef.LootDrop("Manastone", 0.01f),
            new MonsterDef.LootDrop("Mithril chainmail armor", 0.005f),
            new MonsterDef.LootDrop("Mithril chainmail boots", 0.01f),
            new MonsterDef.LootDrop("Mithril chainmail gauntlets", 0.01f),
            new MonsterDef.LootDrop("Mithril chainmail girdle", 0.01f),
            new MonsterDef.LootDrop("Mithril chainmail helmet", 0.01f),
            new MonsterDef.LootDrop("Mithril chainmail leggings", 0.01f),
            new MonsterDef.LootDrop("Collector book", 0.005f),
            new MonsterDef.LootDrop("Rough garnet", 0.001f),
            new MonsterDef.LootDrop("Rough ruby", 0.004f),
            new MonsterDef.LootDrop("Rough carnelian", 0.02f),
            new MonsterDef.LootDrop("Translucent wristband", 0.02f)),
        false,
        0.0f,
        105,
        95,
        95,
        123,
        0,
        95,
        0,
        new int[] {61, 61, 61, 61, 41, 5000, 100, 100, 100, 100, 100, 100},
        90,
        370,
        0,
        1078362112,
        20042,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        37,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d156+120", 1090, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 5, 10381, 1, 12),
            new MonsterDef.Attack("", 0, 5, 10344, 1, 12),
            new MonsterDef.Attack("", 0, 5, 10357, 1, 12),
            new MonsterDef.Attack("", 0, 5, 10386, 1, 12),
            new MonsterDef.Attack("", 0, 45, 10090, 1, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
