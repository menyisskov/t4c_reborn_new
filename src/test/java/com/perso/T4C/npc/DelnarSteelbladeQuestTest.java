package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class DelnarSteelbladeQuestTest {
  @Test
  void villainSkullTurnInConsumesTheStackAndTracksTheTotal() throws Exception {
    Player player = new Player();
    player.setStrength(400);
    assertTrue(InventoryService.add(player, "villain_skull").success());
    assertTrue(InventoryService.add(player, "villain_skull").success());
    DelnarSteelblade npc = new DelnarSteelblade(new NpcContext(null));
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onKeyword(context, "Villain Skull"));
    assertTrue(behavior.onYesNo(context, "delnar_skulls", true));

    assertEquals(0, InventoryService.count(player, "villain_skull"));
    assertEquals(2, player.getQuestFlag("__QUEST_DELNAR_SKULLS_TURNED_IN"));
  }
}
