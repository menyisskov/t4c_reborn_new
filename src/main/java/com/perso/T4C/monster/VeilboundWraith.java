package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterGoldCurve;
import com.perso.T4C.spawn.Spawn;

// Lower-tier trash of The Fading Veil (center 4120,1560 r130, worldZ 0) — a corrupted spirit
// bound to the spreading blight. Reuses the plain Skeleton animation/sound family, distinct from
// the plate-armored Sundered Sentinels. Three extra spawns stand near Ysolde's lair (4100,1580)
// as her corrupted escorts.
@Spawn(type = "Veilbound Wraith", x = 4158, y = 1559, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4327, y = 1731, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4344, y = 1652, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4180, y = 1795, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4391, y = 1631, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4065, y = 1714, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4114, y = 1804, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4227, y = 1720, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4082, y = 1673, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4306, y = 1700, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4014, y = 1762, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4118, y = 1838, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4235, y = 1510, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4176, y = 1811, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4058, y = 1404, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4149, y = 1773, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 3893, y = 1495, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4162, y = 1704, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4104, y = 1776, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4364, y = 1639, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4105, y = 1685, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4067, y = 1741, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4002, y = 1304, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4198, y = 1721, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4297, y = 1730, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4197, y = 1821, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4078, y = 1790, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4155, y = 1656, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4159, y = 1579, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4292, y = 1688, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4129, y = 1534, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4012, y = 1794, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4178, y = 1680, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4204, y = 1755, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 3917, y = 1406, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4168, y = 1642, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4174, y = 1537, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4258, y = 1648, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4233, y = 1657, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4250, y = 1670, z = 0, stationary = false, aggressive = true)
@com.perso.T4C.monster.core.NoRecoveryLoot
public final class VeilboundWraith extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public static final String CANONICAL_NAME = "Veilbound Wraith";

  public VeilboundWraith(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return EndgameMonsterFactory.withWitnessLoot(
        new MonsterDef(
            "Veilbound Wraith",
            "${monster.veilbound_wraith}",
            53400,
            0,
            8,
            190000,
            260,
            560,
            30000L,
            "Skeleton#g",
            "SkeletonA#i",
            "SkeletonC!k",
            SOUND_ATTACK,
            SOUND_DEATH,
            SOUND_HIT,
            MonsterGoldCurve.goldMin(255),
            MonsterGoldCurve.goldMax(255),
            java.util.List.of(
                ),
            false,
            0.0f,
            290,
            340,
            435,
            435,
            0,
            390,
            0,
            new int[] {110, 90, 100, 80, 160, 40, 100, 100, 100, 100, 100, 100},
            255,
            1100,
            0,
            150,
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
            14,
            0,
            true,
            java.util.List.of(new MonsterDef.Attack("1d728+631", 5820, 100, 0, 0, 0)),
            false,
            0,
            java.util.List.of("Veilbound Wraith"),
            java.util.Map.of()),
        false,
        "witness_water_amulet",
        "witness_water_bracelet");
  }
}
