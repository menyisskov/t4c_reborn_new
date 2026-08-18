package com.perso.T4C.monster;

import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;

public final class BerserkerRat extends DataMonster {

  public static final String CANONICAL_NAME = "Berserker Rat";

  public BerserkerRat(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }
}
