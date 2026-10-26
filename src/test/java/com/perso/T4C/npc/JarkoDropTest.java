package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class JarkoDropTest {
  @Test
  void jarkoDropsSpellbookRequiredBySunrockQuest() throws Exception {
    Player player = new Player();
    player.setStrength(400);
    Jarko jarko = new Jarko(new NpcContext(null));

    jarko.javaBehavior().onDeath(new NpcBehaviorContext(jarko, player));

    assertEquals(1, InventoryService.count(player, "jarko_spellbook"));
  }
}
