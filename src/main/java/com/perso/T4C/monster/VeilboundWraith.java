package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// Lower-tier trash of The Fading Veil (center 1420,1560 r130, worldZ 0) — a corrupted spirit
// bound to the spreading blight. Reuses the plain Skeleton animation/sound family, distinct from
// the plate-armored Sundered Sentinels. Three extra spawns stand near Ysolde's lair (1400,1580)
// as her corrupted escorts.
@Spawn(type = "Veilbound Wraith", x = 1520, y = 1520, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1320, y = 1590, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1460, y = 1450, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1360, y = 1660, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1535, y = 1610, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1305, y = 1530, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1490, y = 1650, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1390, y = 1445, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1415, y = 1450, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1522, y = 1485, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1360, y = 1536, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1490, y = 1558, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1329, y = 1539, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1465, y = 1555, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1375, y = 1598, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1481, y = 1612, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1310, y = 1624, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1425, y = 1635, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1447, y = 1648, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1385, y = 1661, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1390, y = 1575, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1410, y = 1575, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Veilbound Wraith", x = 1400, y = 1592, z = 0, stationary = false, aggressive = true)
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
    return new MonsterDef(
        "Veilbound Wraith",
        "${monster.veilbound_wraith}",
        53400,
        0,
        8,
        630000,
        260,
        560,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        485,
        1200,
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
        485,
        1940,
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
        java.util.Map.of());
  }
}
