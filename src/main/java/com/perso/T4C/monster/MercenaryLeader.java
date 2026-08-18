package com.perso.T4C.monster;

import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public final class MercenaryLeader extends NamedEventMonster {

  public MercenaryLeader(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onAttacked(Player p) {

    return Math.random() < 1.0 / 30
        ? message(Math.random() < .5 ? "npc.mercenary.shout.match" : "npc.mercenary.shout.blood")
        : MonsterScriptBridge.Effects.empty();
  }

  @Override
  public MonsterScriptBridge.Effects onDeath(Player p) {

    return messageAndSpell(
        "npc.mercenary.leader.death", "spell.mercenary_leader_defeat_flag_spell");
  }
}
