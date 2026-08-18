package com.perso.T4C.monster;

import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public final class Bloodlust extends NamedEventMonster {

  public Bloodlust(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onDeath(Player p) {

    if (p != null) {

      p.setQuestFlag("USER_HAS_SLAIN_BLOODLUST", 1);

      if (Math.random() < .75) {

        InventoryService.add(p, "bloodstone_ring");

        if (Math.random() < .25) InventoryService.add(p, "essence_of_bloodlust");

      } else if (Math.random() < .25) InventoryService.add(p, "essence_of_bloodlust");
    }

    return MonsterScriptBridge.Effects.empty();
  }
}
