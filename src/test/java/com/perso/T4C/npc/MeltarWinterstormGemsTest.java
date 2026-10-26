package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class MeltarWinterstormGemsTest {
  @Test
  void gemsKeywordTurnsInRoughAndCutGemsForTheirListedExperience() throws Exception {
    Player player = new Player();
    player.setStrength(400);
    assertTrue(InventoryService.add(player, "rough_agate").success());
    assertTrue(InventoryService.add(player, "finely_cut_diamond").success());
    MeltarWinterstorm npc = new MeltarWinterstorm(new NpcContext(null));
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    long before = player.getCurrentXp();
    assertTrue(behavior.onKeyword(context, "Gems"));
    assertTrue(behavior.onYesNo(context, "meltar_gems", true));

    assertEquals(0, InventoryService.count(player, "rough_agate"));
    assertEquals(0, InventoryService.count(player, "finely_cut_diamond"));
    assertEquals(
        Math.round(30_000 * com.perso.T4C.config.GameConstants.SERVER_XP_RATE),
        player.getCurrentXp() - before);
  }
}
