package com.perso.T4C.monster;

import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public final class CrazedNurse extends NamedEventMonster {

  public CrazedNurse(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onHit(Player p) {

    return Math.random() < .02
        ? message("npc.crazednurse.shout." + (int) (Math.random() * 4))
        : MonsterScriptBridge.Effects.empty();
  }

  @Override
  public MonsterScriptBridge.Effects onAttackHit(Player p) {

    return onHit(p);
  }
}
