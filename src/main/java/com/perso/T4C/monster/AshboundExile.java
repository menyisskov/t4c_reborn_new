package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Ashbound Exile", x = 5849, y = 1313, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5632, y = 1373, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5727, y = 1586, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5875, y = 1543, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5847, y = 1470, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5597, y = 1260, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5589, y = 1279, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5606, y = 1303, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5723, y = 1635, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5685, y = 1289, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5720, y = 1358, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5702, y = 1635, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5506, y = 1427, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5531, y = 1375, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5664, y = 1442, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5530, y = 1458, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5740, y = 1572, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5816, y = 1300, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5848, y = 1438, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5847, y = 1263, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5760, y = 1605, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5715, y = 1474, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5473, y = 1335, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5818, y = 1440, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5900, y = 1459, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5798, y = 1456, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5703, y = 1198, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5599, y = 1496, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5641, y = 1504, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5642, y = 1271, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5755, y = 1366, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5658, y = 1256, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5748, y = 1401, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5837, y = 1584, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5490, y = 1386, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5702, y = 1427, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5514, y = 1408, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5680, y = 1491, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5701, y = 1527, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5689, y = 1508, z = 0, stationary = false, aggressive = true)
@com.perso.T4C.monster.core.NoRecoveryLoot
public final class AshboundExile extends DataMonster {
  public static final String CANONICAL_NAME = "Ashbound Exile";

  public AshboundExile(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return EndgameMonsterFactory.withWitnessLoot(
        EndgameMonsterFactory.create(
            CANONICAL_NAME,
            "ashbound_exile",
            SunderedSentinel.definition(),
            315,
            72000,
            355000,
            620,
            false),
        false,
        "witness_earth_amulet",
        "witness_earth_bracelet",
        "witness_earth_signet",
        "witness_earth_tiara");
  }
}
