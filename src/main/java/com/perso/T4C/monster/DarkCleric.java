package com.perso.T4C.monster;

import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public final class DarkCleric extends NamedEventMonster {

  public DarkCleric(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onAttacked(Player p) {

    return Math.random() < 1.0 / 41
        ? message("npc.darkcleric.shout." + (int) (Math.random() * 4))
        : MonsterScriptBridge.Effects.empty();
  }

  @Override
  public MonsterScriptBridge.Effects onDeath(Player p) {

    return message("npc.darkcleric.shout.death");
  }
}
