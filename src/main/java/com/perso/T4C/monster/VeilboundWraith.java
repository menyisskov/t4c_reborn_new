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
@Spawn(type = "Veilbound Wraith", x = 4220, y = 1520, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4020, y = 1590, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4160, y = 1450, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4060, y = 1660, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4235, y = 1610, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4140, y = 1540, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4190, y = 1650, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4131, y = 1535, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4115, y = 1450, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4222, y = 1485, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4151, y = 1535, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4190, y = 1558, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4141, y = 1552, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4165, y = 1555, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4075, y = 1598, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4181, y = 1612, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4010, y = 1624, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4125, y = 1635, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4147, y = 1648, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4085, y = 1661, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4090, y = 1575, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4110, y = 1575, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 4100, y = 1592, z = 0, stationary = false, aggressive = true)
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
                new MonsterDef.LootDrop("serious_healing_potion", 0.08f),
                new MonsterDef.LootDrop("potion_of_mana", 0.1f),
                new MonsterDef.LootDrop("mana_elixir", 0.03f)),
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
