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
@Spawn(type = "Moonlit Stalker", x = 3926, y = 1379, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3744, y = 1429, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3932, y = 1546, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3896, y = 1261, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3837, y = 1400, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3925, y = 1298, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3778, y = 1503, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3920, y = 1449, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4147, y = 1535, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3775, y = 1469, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3868, y = 1431, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4174, y = 1459, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3872, y = 1220, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3769, y = 1400, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3934, y = 1503, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3992, y = 1430, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3840, y = 1472, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3740, y = 1455, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3960, y = 1310, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3763, y = 1282, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3870, y = 1388, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3970, y = 1434, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3971, y = 1238, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3953, y = 1181, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3936, y = 1410, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3844, y = 1196, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3781, y = 1388, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3898, y = 1206, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3821, y = 1351, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4012, y = 1389, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4155, y = 1475, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3851, y = 1307, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4072, y = 1362, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3736, y = 1337, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3870, y = 1540, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3965, y = 1219, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4041, y = 1353, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4045, y = 1378, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 3784, y = 1311, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4075, y = 1403, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4150, y = 1595, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4170, y = 1595, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonlit Stalker", x = 4160, y = 1613, z = 0, stationary = false, aggressive = true)
@com.perso.T4C.monster.core.NoRecoveryLoot
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
                ),
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
