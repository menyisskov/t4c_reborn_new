package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Dark Warlord", x = 448, y = 536, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Warlord", x = 465, y = 502, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Warlord", x = 467, y = 529, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Warlord", x = 470, y = 554, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Warlord", x = 477, y = 542, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Warlord", x = 491, y = 494, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Warlord", x = 498, y = 580, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Warlord", x = 502, y = 470, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Warlord", x = 507, y = 560, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Warlord", x = 519, y = 507, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Warlord", x = 528, y = 557, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Warlord", x = 537, y = 528, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Dark Warlord", x = 559, y = 525, z = 1, stationary = false, aggressive = true)
public final class DarkWarlord extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Dark Warlord";

  public DarkWarlord(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Dark Warlord",
        "${monster.dark_warlord}",
        2086,
        0,
        7,
        7855,
        93,
        211,
        30000L,
        "Demon#i",
        "DemonA#i",
        "DemonC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        134,
        412,
        java.util.List.of(
            new MonsterDef.LootDrop("Twisted key", 0.03f),
            new MonsterDef.LootDrop("Mana prism", 0.01f),
            new MonsterDef.LootDrop("High metal hand axe", 0.005f),
            new MonsterDef.LootDrop("Rough garnet", 0.001f),
            new MonsterDef.LootDrop("Rough ruby", 0.004f),
            new MonsterDef.LootDrop("Rough carnelian", 0.02f),
            new MonsterDef.LootDrop("Gem of the Immortal", 0.07f)),
        false,
        0.0f,
        90,
        82,
        82,
        105,
        0,
        82,
        0,
        new int[] {93, 93, 93, 93, 93, 5000, 100, 100, 100, 100, 100, 100},
        75,
        310,
        0,
        1078099968,
        20013,
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
            new MonsterDef.Attack("1d119+92", 910, 45, 0, 0, 0),
            new MonsterDef.Attack("", 0, 4, 10389, 0, 12),
            new MonsterDef.Attack("", 0, 3, 10391, 0, 12),
            new MonsterDef.Attack("", 0, 35, 10374, 0, 12),
            new MonsterDef.Attack("", 0, 20, 10119, 0, 12),
            new MonsterDef.Attack("", 0, 3, 10382, 0, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
