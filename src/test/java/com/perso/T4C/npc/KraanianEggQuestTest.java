package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.DeepOneBoss;
import com.perso.T4C.monster.r279Zzartgax;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class KraanianEggQuestTest {
  @Test
  void eggRodAndTwoLevelGatesLeadToZzartgaxReward() throws Exception {
    List<String> summons = new ArrayList<>();
    NpcScriptRuntime.setSummonCallback(
        (monster, x, y, z) -> {
          summons.add(monster);
          return true;
        });
    try {
      Player player = new Player();
      player.setLevel(20);
      player.setStrength(400);
      player.addGold(32000);
      assertNotNull(ItemRegistry.findByKey("mana_rod"));
      assertTrue(InventoryService.add(player, "kraanian_egg").success());

      Zhakar zhakar = new Zhakar(new NpcContext(null));
      NpcBehaviorContext zhakarContext = new NpcBehaviorContext(zhakar, player);
      zhakar.javaBehavior().onKeyword(zhakarContext, "Egg");
      zhakar.javaBehavior().onYesNo(zhakarContext, "kraanian_egg", true);
      assertEquals(0, InventoryService.count(player, "kraanian_egg"));
      assertEquals(1, player.getQuestFlag("__QUEST_KRAANIAN_EGG_STAGE"));
      zhakar.javaBehavior().onKeyword(zhakarContext, "Egg");
      assertEquals(1, player.getQuestFlag("__QUEST_KRAANIAN_EGG_STAGE"));

      player.setLevel(21);
      zhakar.javaBehavior().onKeyword(zhakarContext, "Egg");
      assertEquals(2, player.getQuestFlag("__QUEST_KRAANIAN_EGG_STAGE"));
      DeepOneBoss boss = new DeepOneBoss(DeepOneBoss.definition(), 0, 0);
      boss.onDeath(player);
      assertEquals(3, player.getQuestFlag("__QUEST_KRAANIAN_EGG_STAGE"));
      assertEquals("note_deepone", DeepOneBoss.definition().getLoot().get(2).getItem());
      assertTrue(InventoryService.add(player, "note_deepone").success());

      AloysiusStarbolt aloysius = new AloysiusStarbolt(new NpcContext(null));
      NpcBehaviorContext aloysiusContext = new NpcBehaviorContext(aloysius, player);
      aloysius.javaBehavior().onKeyword(aloysiusContext, "Deep One");
      aloysius.javaBehavior().onYesNo(aloysiusContext, "mana_rod_sale", true);
      assertEquals(0, player.getGold());
      assertEquals(0, InventoryService.count(player, "note_deepone"));
      assertEquals(1, InventoryService.count(player, "mana_rod"));
      zhakar.javaBehavior().onKeyword(zhakarContext, "Egg");
      assertEquals(0, InventoryService.count(player, "mana_rod"));
      assertEquals(5, player.getQuestFlag("__QUEST_KRAANIAN_EGG_STAGE"));
      zhakar.javaBehavior().onKeyword(zhakarContext, "Egg");
      assertEquals(List.of(), summons);

      player.setLevel(22);
      zhakar.javaBehavior().onKeyword(zhakarContext, "Egg");
      assertEquals(List.of("Zzartgax"), summons);
      assertEquals(6, player.getQuestFlag("__QUEST_KRAANIAN_EGG_STAGE"));
      long xp = player.getCurrentXp();
      new r279Zzartgax(r279Zzartgax.definition(), 0, 0).onDeath(player);
      assertEquals(1, InventoryService.count(player, "ethereal_amulet"));
      assertTrue(player.getCurrentXp() > xp);
      assertEquals(1, player.getQuestFlag("__QUEST_KRAANIAN_EGG_DONE"));
    } finally {
      NpcScriptRuntime.setSummonCallback(null);
    }
  }
}
