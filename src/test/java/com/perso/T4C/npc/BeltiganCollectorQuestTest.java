package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class BeltiganCollectorQuestTest {
  @Test
  void goodPathExchangesTwoLionRingsForOneCollectorReward() throws Exception {
    BeltiganWhitesword npc = new BeltiganWhitesword(new NpcContext(null));
    NpcBehavior behavior = npc.javaBehavior();
    Player player = new Player();
    player.setStrength(400);
    player.setKarma(100);
    assertTrue(InventoryService.add(player, "ring_of_the_lion").success());
    assertTrue(InventoryService.add(player, "ring_of_the_lion").success());
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onKeyword(context, "Ring of the Lion"));
    assertTrue(behavior.onYesNo(context, "beltigan_rings", true));

    assertEquals(0, InventoryService.count(player, "ring_of_the_lion"));
    assertEquals(
        1,
        InventoryService.count(player, "eye_of_the_tiger")
            + InventoryService.count(player, "crystal_sword_of_might"));
  }
}
