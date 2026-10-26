package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Swamp Hydra", x = 3450, y = 2790, z = 0, stationary = false, aggressive = true)
public final class SwampHydra extends MythicCreatureMonster {
  public SwampHydra(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  public static MonsterDef definition() {
    return MythicCreatures.definition(MythicCreatures.HYDRA);
  }
}
