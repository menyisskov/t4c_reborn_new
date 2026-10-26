package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NoRecoveryLoot;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "Stormbound Pegasus",
    x = 2900,
    y = 2960,
    z = 0,
    stationary = false,
    aggressive = true)
@NoRecoveryLoot
public final class StormboundPegasus extends DataMonster {
  public StormboundPegasus(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  public static MonsterDef definition() {
    return MythicBeasts.definition(MythicBeasts.PEGASUS);
  }
}
