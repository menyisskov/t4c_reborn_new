package com.perso.T4C.monster;

import com.perso.T4C.divine.DivineAvatar;
import com.perso.T4C.divine.DivinePantheon;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Divine Osiris", x = 5800, y = 2270, z = 0, stationary = false, aggressive = false)
public final class DivineOsiris extends DivineAvatar {
  public DivineOsiris(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  public static MonsterDef definition() {
    return DivinePantheon.definition("osiris");
  }
}
