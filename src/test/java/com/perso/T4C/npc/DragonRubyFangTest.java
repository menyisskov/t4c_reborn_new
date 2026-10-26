package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class DragonRubyFangTest {
  @Test
  void dragonGivesOneFangAtATimeAfterTheTombRaiderQuest() throws Exception {
    Dragon dragon = new Dragon(new NpcContext(null));
    NpcBehavior behavior = dragon.javaBehavior();
    Player player = new Player();
    player.setStrength(400);
    NpcBehaviorContext context = new NpcBehaviorContext(dragon, player);

    assertTrue(behavior.onKeyword(context, "ruby dragon fangs"));
    assertEquals(0, InventoryService.count(player, "ruby_dragon_fang"));

    player.setQuestFlag("__QUEST_DRAGON_QUEST_COMPLETED", 2);
    assertTrue(behavior.onKeyword(context, "ruby dragon fangs"));
    assertTrue(behavior.onKeyword(context, "ruby dragon fangs"));
    assertEquals(1, InventoryService.count(player, "ruby_dragon_fang"));

    player.getInventory().clear();
    assertTrue(behavior.onKeyword(context, "ruby dragon fangs"));
    assertEquals(1, InventoryService.count(player, "ruby_dragon_fang"));
  }
}
