package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Rift Wraith", x = 5590, y = 2080, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5605, y = 2080, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5620, y = 2080, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5635, y = 2080, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5650, y = 2080, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5590, y = 2100, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5605, y = 2100, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5620, y = 2100, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5635, y = 2100, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5650, y = 2100, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5665, y = 2100, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5598, y = 2090, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5613, y = 2090, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5628, y = 2090, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5643, y = 2090, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5658, y = 2090, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5598, y = 2110, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5613, y = 2110, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Rift Wraith", x = 5628, y = 2110, z = 0, stationary = false, aggressive = true)
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
