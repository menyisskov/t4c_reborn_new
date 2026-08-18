package com.perso.T4C.monster;

import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;

public final class CENTAURCHAMPION1 extends DataMonster {

  public static final String CANONICAL_NAME = "CENTAURCHAMPION1";

  public CENTAURCHAMPION1(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }
}
