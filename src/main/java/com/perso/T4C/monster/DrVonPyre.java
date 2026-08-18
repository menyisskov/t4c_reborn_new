package com.perso.T4C.monster;

import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public final class DrVonPyre extends NamedEventMonster {

  public DrVonPyre(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player p) {

    return Math.random() < 1.0 / 40
        ? message("npc.drvonpyre.shout")
        : MonsterScriptBridge.Effects.empty();
  }
}
