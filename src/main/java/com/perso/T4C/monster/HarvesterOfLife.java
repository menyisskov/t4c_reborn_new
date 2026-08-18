package com.perso.T4C.monster;

import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;

public final class HarvesterOfLife extends NamedEventMonster {

  public HarvesterOfLife(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }
}
