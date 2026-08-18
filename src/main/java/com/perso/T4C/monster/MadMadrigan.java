package com.perso.T4C.monster;

import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public final class MadMadrigan extends NamedEventMonster {

  public MadMadrigan(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onSpawn(Player player) {

    return selfSpell("spell.mob_invisibility_spell");
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player player) {

    return Math.random() < 1.0 / 30
        ? message(Math.random() < .5 ? "npc.madmadrigan.shout.0" : "npc.madmadrigan.shout.1")
        : MonsterScriptBridge.Effects.empty();
  }
}
