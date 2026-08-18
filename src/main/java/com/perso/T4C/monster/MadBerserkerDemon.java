package com.perso.T4C.monster;

import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public final class MadBerserkerDemon extends NamedEventMonster {

  public MadBerserkerDemon(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onSpawn(Player p) {

    return selfSpell("spell.npc_cantrip_pentacle");
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player p) {

    int r = (int) (Math.random() * 21);

    return r < 2 ? message("npc.madberserker.shout." + r) : MonsterScriptBridge.Effects.empty();
  }
}
