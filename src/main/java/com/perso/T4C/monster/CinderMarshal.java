package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "The Cinder Marshal",
    x = 4360,
    y = 2630,
    z = 0,
    stationary = false,
    aggressive = true)
public final class CinderMarshal extends DataMonster {
  public static final String CANONICAL_NAME = "The Cinder Marshal";

  public CinderMarshal(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return EndgameMonsterFactory.create(
        CANONICAL_NAME,
        "cinder_marshal",
        SirCaradocTheSunderedKnight.definition(),
        290,
        120000,
        8500000,
        850,
        true);
  }
}
