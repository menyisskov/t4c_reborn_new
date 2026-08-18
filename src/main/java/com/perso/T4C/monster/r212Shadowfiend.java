package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r212Shadowfiend extends DataMonster {
  public static final String SOUND_ATTACK = "Demon Attack.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

  public r212Shadowfiend(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Shadowfiend",
        "${monster.shadowfiend}",
        1497,
        0,
        6,
        4869,
        70,
        159,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        107,
        330,
        java.util.List.of(
            new MonsterDef.LootDrop("Polished bone key", 0.03f),
            new MonsterDef.LootDrop("Manastone", 0.01f),
            new MonsterDef.LootDrop("Mithril chainmail boots", 0.01f),
            new MonsterDef.LootDrop("Mithril chainmail armor", 0.005f),
            new MonsterDef.LootDrop("Mithril chainmail gauntlets", 0.01f),
            new MonsterDef.LootDrop("Mithril chainmail girdle", 0.01f),
            new MonsterDef.LootDrop("Mithril chainmail helmet", 0.01f),
            new MonsterDef.LootDrop("Mithril chainmail leggings", 0.01f),
            new MonsterDef.LootDrop("Adamantite blade", 0.001f),
            new MonsterDef.LootDrop("Rough moonstone", 0.001f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough agate", 0.02f),
            new MonsterDef.LootDrop("Finely cut agate", 0.01f),
            new MonsterDef.LootDrop("Finely cut diamond", 0.002f),
            new MonsterDef.LootDrop("Finely cut moonstone", 5.0E-4f)),
        false,
        0.0f,
        75,
        68,
        68,
        87,
        0,
        68,
        0,
        new int[] {78, 78, 78, 78, 105, 5000, 100, 100, 100, 100, 100, 100},
        60,
        250,
        0,
        1077805056,
        21042,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        43,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d90+69", 730, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 5, 10347, 5, 12),
            new MonsterDef.Attack("", 0, 25, 10386, 3, 10),
            new MonsterDef.Attack("", 0, 15, 10384, 3, 10),
            new MonsterDef.Attack("", 0, 10, 10096, 3, 10),
            new MonsterDef.Attack("", 0, 3, 10274, 3, 10),
            new MonsterDef.Attack("", 0, 5, 10357, 3, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
