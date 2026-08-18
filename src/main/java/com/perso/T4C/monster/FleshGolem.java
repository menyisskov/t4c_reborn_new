package com.perso.T4C.monster;

import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import java.util.List;

public final class FleshGolem extends NamedEventMonster {

  public FleshGolem(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onSpawn(Player p) {

    return selfSpell("spell.npc_cantrip_red_wipe");
  }

  @Override
  public MonsterScriptBridge.Effects onAttacked(Player p) {

    if (getHealth() < 181 && Math.random() < .25)
      return new MonsterScriptBridge.Effects(
          List.of("${npc.fleshgolem.shout.last_breath}"),
          List.of("spell.mob_ai_spell_blaze_of_glory"),
          List.of());

    return MonsterScriptBridge.Effects.empty();
  }
}
