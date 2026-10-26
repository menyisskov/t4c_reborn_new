package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.Mordred;
import com.perso.T4C.monster.ObsidianConclaveKnight;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ObsidianConclaveQuestTest {
  @Test
  void knightsSummonMordredWhoseKeyOpensQuestChest() throws Exception {
    List<String> summons = new ArrayList<>();
    NpcScriptRuntime.setSummonCallback(
        (monster, x, y, z) -> {
          summons.add(monster);
          return true;
        });
    try {
      Player player = new Player();
      player.setStrength(400);
      player.setQuestFlag("ADDON_SERAPH_ARMOR_QUEST", 1);
      ObsidianConclaveKnight knight =
          new ObsidianConclaveKnight(ObsidianConclaveKnight.definition(), 0, 0);
      for (int i = 0; i < 10; i++) knight.onDeath(player);
      assertEquals(List.of("MOBMORDRED"), summons);

      Mordred mordred = new Mordred(Mordred.definition(), 0, 0);
      mordred.onDeath(player);
      assertEquals(2, player.getQuestFlag("ADDON_SERAPH_ARMOR_QUEST"));
      assertEquals("mordred_key", Mordred.definition().getLoot().get(0).getItem());

      InventoryService.add(player, "mordred_key");
      MordredChest chest = new MordredChest(new NpcContext(null));
      chest.javaBehavior().onConversationStart(new NpcBehaviorContext(chest, player));
      assertEquals(1, InventoryService.count(player, "seraph_plate"));
      assertEquals(0, InventoryService.count(player, "mordred_key"));
      assertEquals(3, player.getQuestFlag("ADDON_SERAPH_ARMOR_QUEST"));
    } finally {
      NpcScriptRuntime.setSummonCallback(null);
    }
  }
}
