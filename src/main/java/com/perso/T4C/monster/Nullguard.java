package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Nullguard", x = 5426, y = 1686, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5664, y = 1854, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5460, y = 1701, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5554, y = 1843, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5293, y = 1863, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5417, y = 1667, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5484, y = 1672, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5696, y = 1847, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5422, y = 1883, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5251, y = 1803, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5497, y = 1985, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5502, y = 1731, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5411, y = 1779, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5530, y = 1640, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5425, y = 1563, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5630, y = 1615, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5443, y = 1671, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5481, y = 1851, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5697, y = 1831, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5371, y = 1972, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5379, y = 1945, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5631, y = 1631, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5492, y = 2008, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5522, y = 1895, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5429, y = 1756, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5231, y = 1729, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5647, y = 1609, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5289, y = 1920, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5276, y = 1625, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5624, y = 1820, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5391, y = 1803, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5530, y = 1758, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5550, y = 1729, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5607, y = 1944, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5714, y = 1786, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5424, y = 1901, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5521, y = 1960, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5518, y = 1819, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5505, y = 1837, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nullguard", x = 5489, y = 1814, z = 0, stationary = false, aggressive = true)
@com.perso.T4C.monster.core.NoRecoveryLoot
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
