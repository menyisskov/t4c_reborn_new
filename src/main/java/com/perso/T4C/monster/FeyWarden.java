package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// Loyal, still-unmarred trash of The Avalon Wilds (center 1265,1400 r110, worldZ 0). Reuses the
// TreeEnt animation/sound family already used by Forest Guardian — a calm, defensive nature
// spirit, not the corrupted Veil horrors further south. Passive like Forest Guardian: it defends
// the Wilds rather than hunting through them.
@Spawn(type = "Fey Warden", x = 1345, y = 1420, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1195, y = 1450, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1295, y = 1310, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1175, y = 1370, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1325, y = 1470, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1225, y = 1485, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1360, y = 1385, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1170, y = 1415, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1195, y = 1319, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1253, y = 1293, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1315, y = 1306, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1222, y = 1379, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1266, y = 1366, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1321, y = 1364, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1222, y = 1433, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1261, y = 1424, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1325, y = 1423, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1249, y = 1461, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1291, y = 1480, z = 0, stationary = false, aggressive = false)
@Spawn(type = "Fey Warden", x = 1236, y = 1502, z = 0, stationary = false, aggressive = false)
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
        330,
        825,
        java.util.List.of(
            new MonsterDef.LootDrop("light_healing_potion", 0.15f),
            new MonsterDef.LootDrop("healing_potion", 0.08f),
            new MonsterDef.LootDrop("torch", 0.2f)),
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
