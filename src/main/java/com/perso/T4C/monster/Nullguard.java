package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Nullguard", x = 5430, y = 1740, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5445, y = 1740, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5460, y = 1740, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5475, y = 1740, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5490, y = 1740, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5430, y = 1760, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5445, y = 1760, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5460, y = 1760, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5475, y = 1760, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5490, y = 1760, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5505, y = 1760, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5438, y = 1750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5453, y = 1750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5468, y = 1750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5483, y = 1750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5498, y = 1750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5438, y = 1770, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5453, y = 1770, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5468, y = 1770, z = 0, stationary = false, aggressive = true)
public final class Nullguard extends DataMonster {
  public static final String CANONICAL_NAME = "Nullguard";

  public Nullguard(MonsterDef definition, float x, float y) throws GameException {
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
            "nullguard",
            SunderedSentinel.definition(),
            355,
            94000,
            460000,
            800,
            false),
        false,
        "witness_light_amulet",
        "witness_light_bracelet",
        "witness_light_signet",
        "witness_light_tiara");
  }
}
