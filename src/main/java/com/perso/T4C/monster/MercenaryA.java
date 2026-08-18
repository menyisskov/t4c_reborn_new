package com.perso.T4C.monster;

import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public class MercenaryA extends NamedEventMonster {

  public MercenaryA(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onAttacked(Player player) {

    return mercenaryShout();
  }

  private static MonsterScriptBridge.Effects mercenaryShout() {

    return Math.random() < 1.0 / 30
        ? message(Math.random() < .5 ? "npc.mercenary.shout.match" : "npc.mercenary.shout.blood")
        : MonsterScriptBridge.Effects.empty();
  }
}
