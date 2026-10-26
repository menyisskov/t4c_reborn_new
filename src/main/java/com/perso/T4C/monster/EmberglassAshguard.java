package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Emberglass Ashguard", x = 4249, y = 2557, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4409, y = 2736, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4331, y = 2649, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4407, y = 2431, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4358, y = 2379, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4233, y = 2400, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4303, y = 2842, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4461, y = 2515, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4253, y = 2774, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4474, y = 2658, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4188, y = 2770, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4522, y = 2533, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4504, y = 2556, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4232, y = 2634, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4384, y = 2615, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4312, y = 2575, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4158, y = 2693, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4532, y = 2585, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4216, y = 2582, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4173, y = 2601, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4528, y = 2624, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4550, y = 2471, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4135, y = 2644, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4352, y = 2710, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4329, y = 2739, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4206, y = 2563, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4215, y = 2773, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4528, y = 2442, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4340, y = 2448, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4404, y = 2489, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4613, y = 2595, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4329, y = 2435, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4496, y = 2583, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4381, y = 2812, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4406, y = 2808, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4410, y = 2398, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4190, y = 2612, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4336, y = 2704, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4265, y = 2699, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Emberglass Ashguard", x = 4311, y = 2672, z = 0, stationary = false, aggressive = true)
@com.perso.T4C.monster.core.NoRecoveryLoot
public final class EmberglassAshguard extends DataMonster {
  public static final String CANONICAL_NAME = "Emberglass Ashguard";

  public EmberglassAshguard(MonsterDef definition, float x, float y) throws GameException {
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
            "emberglass_ashguard",
            SunderedSentinel.definition(),
            270,
            61000,
            245000,
            550,
            false),
        false,
        "witness_air_amulet",
        "witness_air_bracelet",
        "witness_air_signet",
        "witness_air_tiara");
  }
}
