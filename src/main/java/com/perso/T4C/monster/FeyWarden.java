package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterGoldCurve;
import com.perso.T4C.spawn.Spawn;

// Loyal, still-unmarred trash of The Avalon Wilds (center 3965,1400 r110, worldZ 0). Reuses the
// TreeEnt animation/sound family already used by Forest Guardian — a calm, defensive nature
// spirit, not the corrupted Veil horrors further south. Passive like Forest Guardian: it defends
// the Wilds rather than hunting through them.
@Spawn(type = "Fey Warden", x = 3838, y = 1321, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3780, y = 1278, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3913, y = 1470, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3722, y = 1268, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3798, y = 1215, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3904, y = 1285, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3818, y = 1270, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3656, y = 1331, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3771, y = 1234, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 4064, y = 1377, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3676, y = 1422, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3828, y = 1112, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3927, y = 1347, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3739, y = 1263, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3798, y = 1355, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3856, y = 1134, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3945, y = 1139, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3836, y = 1234, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3937, y = 1265, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3665, y = 1279, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3662, y = 1184, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3794, y = 1471, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3756, y = 1338, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3798, y = 1374, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3817, y = 1464, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3884, y = 1336, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3683, y = 1312, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3854, y = 1353, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3771, y = 1370, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3646, y = 1226, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3917, y = 1227, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3832, y = 1375, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3920, y = 1197, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3730, y = 1407, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3629, y = 1215, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3971, y = 1356, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3923, y = 1280, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3772, y = 1178, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3716, y = 1187, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 3752, y = 1219, z = 0, stationary = false, aggressive = false)
@com.perso.T4C.monster.core.NoRecoveryLoot
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
    return EndgameMonsterFactory.withWitnessLoot(
        new MonsterDef(
            "Fey Warden",
            "${monster.fey_warden}",
            36300,
            0,
            6,
            115000,
            180,
            400,
            30000L,
            "TreeEnt#i",
            "TreeEntA#i",
            "TreeEntC!j",
            SOUND_ATTACK,
            SOUND_DEATH,
            SOUND_HIT,
            MonsterGoldCurve.goldMin(210),
            MonsterGoldCurve.goldMax(210),
            java.util.List.of(
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
            210,
            850,
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
            java.util.Map.of()),
        false,
        "witness_fire_amulet",
        "witness_fire_bracelet");
  }
}
