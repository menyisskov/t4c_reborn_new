package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Goblin Warlord", x = 1023, y = 125, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1023, y = 148, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1027, y = 144, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1033, y = 209, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1042, y = 108, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1054, y = 93, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1153, y = 210, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1741, y = 327, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1796, y = 159, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1796, y = 439, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1837, y = 541, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1888, y = 298, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1892, y = 301, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1896, y = 298, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1949, y = 134, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 1969, y = 313, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 2022, y = 164, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 2070, y = 337, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 2140, y = 279, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 2221, y = 418, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 2309, y = 163, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin Warlord", x = 981, y = 199, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Xarrax", x = 2223, y = 2824, z = 1, stationary = false, aggressive = true)
public final class GoblinWarlord extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public static final String CANONICAL_NAME = "Goblin Warlord";

  public GoblinWarlord(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Goblin Warlord",
        "${monster.goblin_warlord}",
        353,
        0,
        3,
        543,
        17,
        39,
        30000L,
        "Goblin#l",
        "GoblinA#i",
        "GoblinC#o",
        "Taunting Attack.wav",
        "Taunting Dying.wav",
        "Taunting Hit.wav",
        35,
        110,
        java.util.List.of(
            new MonsterDef.LootDrop("Sword of fury", 0.02f),
            new MonsterDef.LootDrop("Flask of Goblin Blood", 0.005f),
            new MonsterDef.LootDrop("Ringmail armor", 5.0E-4f),
            new MonsterDef.LootDrop("Ringmail leggings", 5.0E-4f),
            new MonsterDef.LootDrop("Ringmail helmet", 5.0E-4f)),
        false,
        0.0f,
        35,
        32,
        32,
        39,
        0,
        32,
        0,
        new int[] {112, 56, 84, 84, 84, 5025, 100, 100, 100, 100, 100, 100},
        20,
        90,
        0,
        1076101120,
        20001,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d23+16", 250, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("Xarrax"),
        java.util.Map.of());
  }
}
