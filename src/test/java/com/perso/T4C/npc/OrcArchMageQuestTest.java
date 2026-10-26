package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class OrcArchMageQuestTest {
  @Test
  void killingArchMageUnlocksAloysiusFirstReward() throws Exception {
    Player player = new Player();
    player.setStrength(400);
    Orcarchmage archMage = new Orcarchmage(new NpcContext(null));
    AloysiusStarbolt aloysius = new AloysiusStarbolt(new NpcContext(null));

    archMage.javaBehavior().onDeath(new NpcBehaviorContext(archMage, player));
    assertEquals(1, player.getQuestFlag("__QUEST_KILLED_ORC_MAGUS"));

    aloysius.javaBehavior().onConversationStart(new NpcBehaviorContext(aloysius, player));
    assertEquals(0, player.getQuestFlag("__QUEST_KILLED_ORC_MAGUS"));
    assertTrue(InventoryService.count(player, "platinum_ring") > 0);
    assertTrue(player.getCurrentXp() > 0);
  }
}
