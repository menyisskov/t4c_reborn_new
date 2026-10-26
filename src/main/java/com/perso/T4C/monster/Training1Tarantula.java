package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NoRecoveryLoot;
import com.perso.T4C.monster.core.TrainingChampion;

@NoRecoveryLoot
public final class Training1Tarantula extends TrainingChampion {
  public Training1Tarantula(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  public static MonsterDef definition() {
    return com.perso.T4C.spawn.TrainingGroundContent.boss(1, "Tarantula");
  }
}
