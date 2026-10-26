package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Obsidian Dragon", x = 3360, y = 2635, z = 0, stationary = false, aggressive = true)
public final class ObsidianDragon extends MythicCreatureMonster {
  public ObsidianDragon(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  public static MonsterDef definition() {
    return MythicCreatures.definition(MythicCreatures.DRAGON);
  }
}
