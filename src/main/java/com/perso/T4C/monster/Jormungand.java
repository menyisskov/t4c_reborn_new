package com.perso.T4C.monster;

import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.monster.core.*;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import java.util.List;

public final class Jormungand extends NamedEventMonster {

  public Jormungand(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onDeath(Player p) {

    if (p != null) InventoryService.add(p, "jormungand_soulstone");

    return new MonsterScriptBridge.Effects(
        List.of("${message.jormungand.soulstone}"), List.of(), List.of());
  }
}
