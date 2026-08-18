package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Crypt Stalker", x = 1000, y = 2472, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1111, y = 2250, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1114, y = 2231, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1120, y = 2254, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1124, y = 2235, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1130, y = 2254, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1142, y = 2267, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1143, y = 2267, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1154, y = 2279, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1158, y = 2593, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1161, y = 2315, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1162, y = 2533, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1163, y = 2569, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1163, y = 2603, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1167, y = 2291, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1176, y = 2328, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1179, y = 2554, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1186, y = 2310, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1194, y = 2434, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1195, y = 2465, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1195, y = 2539, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1195, y = 2568, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1203, y = 2326, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1210, y = 2461, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1212, y = 2441, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1214, y = 2339, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1214, y = 2529, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1219, y = 2469, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1230, y = 2432, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1230, y = 2485, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1240, y = 2496, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1247, y = 2417, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1261, y = 2403, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1261, y = 2620, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1263, y = 2633, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1267, y = 2626, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1282, y = 2603, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1310, y = 2606, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1340, y = 2626, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1347, y = 2548, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1350, y = 2567, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1351, y = 2615, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1356, y = 2541, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 1368, y = 2527, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 971, y = 2504, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Crypt Stalker", x = 994, y = 2483, z = 1, stationary = false, aggressive = true)
public final class CryptStalker extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Crypt Stalker";

  public CryptStalker(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Crypt Stalker",
        "${monster.crypt_stalker}",
        936,
        0,
        5,
        2440,
        46,
        105,
        30000L,
        "Spider#f",
        "SpiderA#f",
        "SpiderC#m",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        77,
        236,
        java.util.List.of(),
        false,
        0.0f,
        58,
        53,
        53,
        66,
        0,
        53,
        0,
        new int[] {98, 49, 73, 73, 73, 0, 100, 100, 100, 100, 100, 100},
        43,
        182,
        0,
        1077215232,
        20007,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d60+45", 526, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
