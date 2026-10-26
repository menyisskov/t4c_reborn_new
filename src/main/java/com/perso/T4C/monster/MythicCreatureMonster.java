package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

/** Wider selection area for the large dragon, griffin and hydra sprite silhouettes. */
abstract class MythicCreatureMonster extends DataMonster {
  protected MythicCreatureMonster(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public boolean isMouseOver(float mouseX, float mouseY) {
    return mouseX >= position.x - 110f
        && mouseX <= position.x + 110f
        && mouseY >= position.y - 210f
        && mouseY <= position.y + 20f;
  }
}
