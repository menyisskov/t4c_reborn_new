package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Bronze Griffin", x = 3540, y = 2635, z = 0, stationary = false, aggressive = true)
public final class BronzeGriffin extends MythicCreatureMonster {
  public BronzeGriffin(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  public static MonsterDef definition() {
    return MythicCreatures.definition(MythicCreatures.GRIFFIN);
  }
}
