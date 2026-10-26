package com.perso.T4C.monster;

import com.perso.T4C.divine.DivineAvatar;
import com.perso.T4C.divine.DivinePantheon;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

// Generated entry point; shared combat and loot are owned by DivinePantheon.
@Spawn(type = "Divine Njord", x = 3738, y = 780, z = 0, stationary = false, aggressive = false)
public final class DivineNjord extends DivineAvatar {
  public DivineNjord(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  public static MonsterDef definition() {
    return DivinePantheon.definition("njord");
  }
}
