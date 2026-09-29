package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "The Hush Cantor", x = 5750, y = 1500, z = 0, stationary = false, aggressive = true)
public final class HushCantor extends DataMonster {
  public static final String CANONICAL_NAME = "The Hush Cantor";

  public HushCantor(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return EndgameMonsterFactory.create(
        CANONICAL_NAME,
        "hush_cantor",
        YsoldeTheVeiledMatriarch.definition(),
        340,
        155000,
        15000000,
        1050,
        true);
  }
}
