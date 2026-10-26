package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NoRecoveryLoot;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Galehorn Unicorn", x = 3050, y = 2900, z = 0, stationary = false, aggressive = true)
@NoRecoveryLoot
public final class GalehornUnicorn extends DataMonster {
  public GalehornUnicorn(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  public static MonsterDef definition() {
    return MythicBeasts.definition(MythicBeasts.UNICORN);
  }
}
