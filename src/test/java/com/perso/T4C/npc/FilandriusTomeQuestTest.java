package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.AnthorTheMad;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class FilandriusTomeQuestTest {
  @Test
  void anthorDeathOpensTheTomeTurnIn() throws Exception {
    Player player = new Player();
    player.setLevel(50);
    player.setStrength(400);
    Filandrius filandrius = new Filandrius(new NpcContext(null));
    NpcBehaviorContext context = new NpcBehaviorContext(filandrius, player);

    filandrius.javaBehavior().onKeyword(context, "GET IT");
    assertEquals(1, player.getQuestFlag("ADDON_FILANDRIUS_QUEST_PROGRESS"));

    AnthorTheMad anthor = new AnthorTheMad(AnthorTheMad.definition(), 0, 0);
    anthor.onDeath(player);
    assertEquals(2, player.getQuestFlag("ADDON_FILANDRIUS_QUEST_PROGRESS"));
    assertEquals(1.0f, AnthorTheMad.definition().getLoot().get(0).getChance());

    InventoryService.add(player, "tome_of_arcane_knowledge");
    filandrius.javaBehavior().onConversationStart(context);
    assertEquals(3, player.getQuestFlag("ADDON_FILANDRIUS_QUEST_PROGRESS"));
    assertEquals(0, InventoryService.count(player, "tome_of_arcane_knowledge"));
    assertEquals(1, InventoryService.count(player, "belt_of_unstable_protection"));
    assertEquals(1, InventoryService.count(player, "gem_of_unstable_protection"));
  }
}
