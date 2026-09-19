package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// Upper-tier trash of The Avalon Wilds (center 1265,1400 r110, worldZ 0) — a nocturnal predator,
// still loyal to the unmarred half of the isle. Reuses the Wolf animation/sound family (Ashfang
// Stalker precedent) but is a distinct, higher-level creature. Three extra spawns stand near The
// Verdant Warden's lair (1460,1600) as his loyal escorts making a last stand against the blight.
@Spawn(type = "Moonlit Stalker", x = 1315, y = 1460, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1205, y = 1440, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1335, y = 1360, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1235, y = 1325, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1285, y = 1495, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1180, y = 1375, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1355, y = 1435, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1250, y = 1300, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1229, y = 1299, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1273, y = 1293, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1332, y = 1317, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1195, y = 1349, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1280, y = 1381, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1333, y = 1380, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1222, y = 1409, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1245, y = 1412, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1301, y = 1430, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1260, y = 1444, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1231, y = 1451, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1264, y = 1475, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1450, y = 1595, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1470, y = 1595, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 1460, y = 1613, z = 0, stationary = false, aggressive = true)
public final class MoonlitStalker extends DataMonster {
  public static final String SOUND_ATTACK = "Wolf Attack.wav";
  public static final String SOUND_DEATH = "Wolf Dying.wav";
  public static final String SOUND_HIT = "Wolf Hit.wav";

  public static final String CANONICAL_NAME = "Moonlit Stalker";

  public MoonlitStalker(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Moonlit Stalker",
        "${monster.moonlit_stalker}",
        42900,
        0,
        7,
        430000,
        210,
        460,
        30000L,
        "Wolf#i",
        "WolfA#i",
        "WolfC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        390,
        975,
        java.util.List.of(
            new MonsterDef.LootDrop("healing_potion", 0.1f),
            new MonsterDef.LootDrop("serious_healing_potion", 0.05f),
            new MonsterDef.LootDrop("potion_of_mana", 0.08f)),
        false,
        0.0f,
        330,
        310,
        430,
        195,
        0,
        235,
        0,
        new int[] {140, 100, 120, 70, 110, 90, 100, 100, 100, 100, 100, 100},
        390,
        1560,
        0,
        180,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        2,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d585+507", 4680, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("Moonlit Stalker"),
        java.util.Map.of());
  }
}
