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
    return EndgameMonsterFactory.create(
        CANONICAL_NAME,
        "rift_wraith",
        VeilboundWraith.definition(),
        390,
        130000,
        600000,
        1050,
        false);
  }
}
