package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Rift Wraith", x = 5606, y = 2082, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5618, y = 2142, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5617, y = 1972, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5641, y = 2136, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5425, y = 2022, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5627, y = 2059, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5589, y = 2068, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5608, y = 2108, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5561, y = 2018, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5475, y = 2092, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5502, y = 2040, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5550, y = 1993, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5717, y = 1910, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5672, y = 1907, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5552, y = 2116, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5721, y = 1944, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5693, y = 2116, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5763, y = 2099, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5722, y = 2120, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5457, y = 2075, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5451, y = 2019, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5656, y = 2077, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5684, y = 2050, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5622, y = 1897, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5641, y = 2033, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5421, y = 2167, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5739, y = 2016, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5824, y = 2035, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5499, y = 2099, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5641, y = 1922, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5515, y = 2247, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5709, y = 1995, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5491, y = 2071, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5474, y = 2155, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5527, y = 2124, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5773, y = 1937, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5546, y = 2076, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5745, y = 2047, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5771, y = 2036, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5748, y = 2067, z = 0, stationary = false, aggressive = true)
@com.perso.T4C.monster.core.NoRecoveryLoot
public final class RiftWraith extends DataMonster {
  public static final String CANONICAL_NAME = "Rift Wraith";

  public RiftWraith(MonsterDef definition, float x, float y) throws GameException {
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
            "rift_wraith",
            VeilboundWraith.definition(),
            390,
            130000,
            600000,
            1050,
            false),
        false,
        "witness_dark_amulet",
        "witness_dark_bracelet",
        "witness_dark_signet",
        "witness_dark_tiara");
  }
}
