package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NoRecoveryLoot;

@NoRecoveryLoot
public final class ThrenodySkeleton extends DataMonster {
  public ThrenodySkeleton(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  public static MonsterDef definition() {
    return com.perso.T4C.spawn.TrainingGroundContent.normal(2, "Skeleton");
  }
}
