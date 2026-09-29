package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterGoldCurve;
import com.perso.T4C.spawn.Spawn;

// Loyal, still-unmarred trash of The Avalon Wilds (center 1265,1400 r110, worldZ 0). Reuses the
// TreeEnt animation/sound family already used by Forest Guardian — a calm, defensive nature
// spirit, not the corrupted Veil horrors further south. Passive like Forest Guardian: it defends
// the Wilds rather than hunting through them.
@Spawn(type = "Fey Warden", x = 1193, y = 1390, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1199, y = 1390, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1205, y = 1390, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1211, y = 1390, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1217, y = 1390, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1193, y = 1397, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1199, y = 1397, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1205, y = 1397, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1211, y = 1397, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1217, y = 1397, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1193, y = 1404, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1199, y = 1404, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1205, y = 1404, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1211, y = 1404, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1217, y = 1404, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1193, y = 1411, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1199, y = 1411, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1205, y = 1411, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1211, y = 1411, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1217, y = 1411, z = 0, stationary = false, aggressive = false)
public final class FeyWarden extends DataMonster {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public static final String CANONICAL_NAME = "Fey Warden";

  public FeyWarden(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Fey Warden",
        "${monster.fey_warden}",
        36300,
        0,
        6,
        300000,
        180,
        400,
        30000L,
        "TreeEnt#i",
        "TreeEntA#i",
        "TreeEntC!j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        MonsterGoldCurve.goldMin(330),
        MonsterGoldCurve.goldMax(330),
        java.util.List.of(
            new MonsterDef.LootDrop("light_healing_potion", 0.15f),
            new MonsterDef.LootDrop("torch", 0.2f),
            // T4C-0021: light-flavor source for the Ancient Celestial/Empyrean armor sets
            // (ArmorSetGenerator) - previously generated with zero acquisition path. Rates are
            // well under the boss-tier sources' (0.025f/0.012f) since Fey Warden is a common,
            // 20-point spawn rather than a unique boss.
            new MonsterDef.LootDrop("ancient_celestial_light_armor", 0.006f),
            new MonsterDef.LootDrop("ancient_celestial_light_boots", 0.006f),
            new MonsterDef.LootDrop("ancient_celestial_light_gauntlets", 0.006f),
            new MonsterDef.LootDrop("ancient_celestial_light_helmet", 0.006f),
            new MonsterDef.LootDrop("ancient_celestial_light_leggings", 0.006f),
            new MonsterDef.LootDrop("ancient_celestial_light_protector", 0.006f),
            new MonsterDef.LootDrop("empyrean_light_armor", 0.003f),
            new MonsterDef.LootDrop("empyrean_light_boots", 0.003f),
            new MonsterDef.LootDrop("empyrean_light_gauntlets", 0.003f),
            new MonsterDef.LootDrop("empyrean_light_helmet", 0.003f),
            new MonsterDef.LootDrop("empyrean_light_leggings", 0.003f),
            new MonsterDef.LootDrop("empyrean_light_protector", 0.003f)),
        false,
        0.0f,
        300,
        280,
        280,
        230,
        0,
        300,
        0,
        new int[] {130, 140, 110, 70, 60, 150, 100, 100, 100, 100, 100, 100},
        330,
        1320,
        0,
        200,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -50,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d495+430", 3960, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("Fey Warden"),
        java.util.Map.of());
  }
}
