package com.perso.T4C.npc;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class RitekeeperEdrinTest {
  @Test
  void preparesBothAlignmentRoutesWithoutGrantingVictoryOrRebirth() throws Exception {
    RitekeeperEdrin npc = new RitekeeperEdrin(new NpcContext(null));
    Player good = new Player();
    good.setLevel(75);
    good.setKarma(100);
    NpcBehaviorContext goodContext = new NpcBehaviorContext(npc, good);

    assertTrue(npc.javaBehavior().onKeyword(goodContext, "trial"));
    assertEquals(2628 * GRID_W, good.getCoordinates().getX());
    assertEquals(2456 * GRID_H, good.getCoordinates().getY());
    assertEquals(2, good.getCoordinates().getZ());
    assertEquals(1, good.getQuestFlag("__QUEST_FIXED_ALIGNMENT"));
    assertEquals(2, good.getQuestFlag("__QUEST_ISLAND_ACCESS"));
    assertEquals(1, good.getQuestFlag("__FLAG_CONVERSATION_WITH_ORACLE"));
    assertTrue(good.getInventory().contains("item.trial_key"));
    assertTrue(good.getInventory().contains("item.scroll_of_stonecrest"));
    assertEquals(0, good.getQuestFlag("__FLAG_USER_HAS_DEFEATED_ASSISTANT"));
    assertEquals(0, good.getQuestFlag("__FLAG_NUMBER_OF_REMORTS"));

    Player evil = new Player();
    evil.setLevel(75);
    NpcBehaviorContext evilContext = new NpcBehaviorContext(npc, evil);
    assertTrue(npc.javaBehavior().onKeyword(evilContext, "shadow"));
    assertTrue(npc.javaBehavior().onKeyword(evilContext, "trial"));
    assertEquals(-100, evil.getKarma());
    assertEquals(-1, evil.getQuestFlag("__QUEST_FIXED_ALIGNMENT"));
    assertEquals(2660 * GRID_W, evil.getCoordinates().getX());
    assertEquals(2424 * GRID_H, evil.getCoordinates().getY());
  }

  @Test
  void oracleHandoffRequiresAnEarnedAssistantVictory() throws Exception {
    RitekeeperEdrin npc = new RitekeeperEdrin(new NpcContext(null));
    Player player = new Player();
    player.setLevel(75);
    player.setKarma(100);
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(npc.javaBehavior().onKeyword(context, "oracle"));
    assertEquals(0, player.getCoordinates().getZ());
    player.setQuestFlag("__FLAG_USER_HAS_DEFEATED_ASSISTANT", 1);
    assertTrue(npc.javaBehavior().onKeyword(context, "oracle"));
    assertEquals(2721 * GRID_W, player.getCoordinates().getX());
    assertEquals(2192 * GRID_H, player.getCoordinates().getY());
    assertEquals(2, player.getCoordinates().getZ());

    Oracle oracle = new Oracle(new NpcContext(null));
    NpcBehaviorContext oracleContext = new NpcBehaviorContext(oracle, player);
    assertTrue(oracle.javaBehavior().onKeyword(oracleContext, "ready to be reborn"));
    assertEquals("REBIRTH", oracleContext.pendingYesNo());
  }
}
