package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Ashbound Exile", x = 5660, y = 1390, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5675, y = 1390, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5690, y = 1390, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5705, y = 1390, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5720, y = 1390, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5660, y = 1410, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5675, y = 1410, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5690, y = 1410, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5705, y = 1410, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5720, y = 1410, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5735, y = 1410, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5668, y = 1400, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5683, y = 1400, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5698, y = 1400, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5713, y = 1400, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5728, y = 1400, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5668, y = 1420, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5683, y = 1420, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Ashbound Exile", x = 5698, y = 1420, z = 0, stationary = false, aggressive = true)
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
