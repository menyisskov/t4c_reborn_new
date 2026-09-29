package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterGoldCurve;
import com.perso.T4C.spawn.Spawn;

// Upper-tier trash of The Avalon Wilds (center 3965,1400 r110, worldZ 0) — a nocturnal predator,
// threatening the fey groves. Reuses the Wolf animation/sound family (Ashfang
// Stalker precedent) but is a distinct, higher-level creature. Three extra spawns stand near The
// Verdant Warden's lair (4160,1600) as his loyal escorts making a last stand against the blight.
@Spawn(type = "Moonlit Stalker", x = 3938, y = 1337, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3944, y = 1337, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3950, y = 1337, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3956, y = 1337, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3962, y = 1337, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3938, y = 1353, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3944, y = 1353, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3950, y = 1353, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3956, y = 1353, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3962, y = 1353, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3988, y = 1382, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3994, y = 1382, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4000, y = 1382, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4006, y = 1382, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4012, y = 1382, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3988, y = 1398, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3994, y = 1398, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4000, y = 1398, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4006, y = 1398, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4012, y = 1398, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4150, y = 1595, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4170, y = 1595, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4160, y = 1613, z = 0, stationary = false, aggressive = true)
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
    return EndgameMonsterFactory.withWitnessLoot(
        new MonsterDef(
            "Moonlit Stalker",
            "${monster.moonlit_stalker}",
            42900,
            0,
            7,
            150000,
            210,
            460,
            30000L,
            "Wolf#i",
            "WolfA#i",
            "WolfC!n",
            SOUND_ATTACK,
            SOUND_DEATH,
            SOUND_HIT,
            MonsterGoldCurve.goldMin(225),
            MonsterGoldCurve.goldMax(225),
            java.util.List.of(
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
            225,
            950,
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
            java.util.Map.of()),
        false,
        "witness_fire_signet",
        "witness_fire_tiara");
  }
}
