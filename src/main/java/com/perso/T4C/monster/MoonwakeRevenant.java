package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Moonwake Revenant", x = 3885, y = 890, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 4041, y = 768, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3999, y = 808, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3915, y = 750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3730, y = 717, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3816, y = 870, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3965, y = 905, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3850, y = 891, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3799, y = 797, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3999, y = 779, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3880, y = 817, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 4078, y = 639, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 4133, y = 765, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3859, y = 840, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3808, y = 655, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 4048, y = 847, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3823, y = 613, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3888, y = 682, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3986, y = 562, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3788, y = 616, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3924, y = 673, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3797, y = 890, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3973, y = 769, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3942, y = 693, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3970, y = 798, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3843, y = 686, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3776, y = 653, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3860, y = 769, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3867, y = 902, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3834, y = 595, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 4067, y = 748, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3834, y = 837, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 4104, y = 770, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3982, y = 588, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3844, y = 664, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3864, y = 813, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3877, y = 771, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3946, y = 614, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3943, y = 598, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Moonwake Revenant", x = 3914, y = 620, z = 0, stationary = false, aggressive = true)
@com.perso.T4C.monster.core.NoRecoveryLoot
public final class MoonwakeRevenant extends DataMonster {
  public static final String CANONICAL_NAME = "Moonwake Revenant";

  public MoonwakeRevenant(MonsterDef definition, float x, float y) throws GameException {
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
            "moonwake_revenant",
            VeilboundWraith.definition(),
            230,
            43000,
            175000,
            420,
            false),
        false,
        "witness_water_signet",
        "witness_water_tiara");
  }
}
