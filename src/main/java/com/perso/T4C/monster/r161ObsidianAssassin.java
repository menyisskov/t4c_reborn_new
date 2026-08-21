package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Obsidian Assassin", x = 1074, y = 2615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Obsidian Assassin", x = 1083, y = 2617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Obsidian Assassin", x = 1095, y = 2603, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Obsidian Assassin", x = 1115, y = 2574, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Obsidian Assassin", x = 1309, y = 2617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Obsidian Assassin", x = 1323, y = 2634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Obsidian Assassin", x = 1338, y = 2615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Obsidian Assassin", x = 1373, y = 2670, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Obsidian Assassin", x = 1394, y = 2673, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Obsidian Assassin", x = 1406, y = 2644, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Obsidian Assassin", x = 1411, y = 2706, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Obsidian Assassin", x = 1449, y = 2700, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Obsidian Assassin", x = 1460, y = 2734, z = 2, stationary = false, aggressive = true)
public final class r161ObsidianAssassin extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

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
        "ThiefC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
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
