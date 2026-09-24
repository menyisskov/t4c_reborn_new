package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.quest.QuestService;
import org.junit.jupiter.api.Test;

/** T4C-0035: Anchorite Rowan performs the same rebirth rite the Oracle does (see
 * OracleRebirthTest.java for the equivalent Oracle coverage), gated on completing
 * quest/definition/TheWakingRite.java instead of the Oracle's own "defeated the assistant" flag -
 * a deliberately independent, Avalon-only shortcut so a proven character never has to re-trek to
 * the Oracle's dungeon for a later rebirth. */
class AnchoriteRowanTest {
  private static Player eligiblePlayer() {
    Player player = new Player();
    player.setLevel(150);
    player.setQuestFlag(
        QuestService.statusFlag(QuestRegistry.findById("the_waking_rite")),
        QuestService.STATUS_COMPLETED);
    return player;
  }

  @Test
  void offersRebirthConfirmationToAPlayerWhoCompletedTheWakingRite() throws Exception {
    AnchoriteRowan npc = new AnchoriteRowan(new NpcContext(null));
    Player player = eligiblePlayer();
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onKeyword(context, "ready reborn"));

    assertEquals("REBIRTH", context.pendingYesNo());
  }

  @Test
  void refusesRebirthWithoutCompletingTheWakingRiteFirst() throws Exception {
    AnchoriteRowan npc = new AnchoriteRowan(new NpcContext(null));
    Player player = new Player();
    player.setLevel(150);
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onKeyword(context, "ready reborn"));

    assertNull(context.pendingYesNo());
  }

  @Test
  void refusesRebirthBelowTheRequiredLevel() throws Exception {
    AnchoriteRowan npc = new AnchoriteRowan(new NpcContext(null));
    Player player = eligiblePlayer();
    player.setLevel(74);
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    behavior.onKeyword(context, "ready reborn");

    assertNull(context.pendingYesNo());
  }

  @Test
  void confirmingRebirthIncrementsTheCounterWithoutForcingATeleport() throws Exception {
    AnchoriteRowan npc = new AnchoriteRowan(new NpcContext(null));
    Player player = eligiblePlayer();
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onYesNo(context, "REBIRTH", true));

    assertEquals(1, player.getRebirthCount());
    assertEquals(1, player.getLevel());
  }

  @Test
  void refusesRebirthOnceTheRebirthLimitIsReached() throws Exception {
    AnchoriteRowan npc = new AnchoriteRowan(new NpcContext(null));
    Player player = eligiblePlayer();
    player.setLevel(400);
    int limit = com.perso.T4C.config.GameConstants.REBIRTH_MAX_REMORTS;
    player.setQuestFlag("__FLAG_NUMBER_OF_REMORTS", limit);
    player.setRebirthCount(limit);
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    behavior.onKeyword(context, "ready reborn");
    assertNull(context.pendingYesNo());
    assertTrue(behavior.onYesNo(context, "REBIRTH", true));

    assertEquals(400, player.getLevel());
    assertEquals(limit, player.getRebirthCount());
  }

  @Test
  void unrelatedKeywordsFallThroughToTheDeclarativeTopics() throws Exception {
    AnchoriteRowan npc = new AnchoriteRowan(new NpcContext(null));
    Player player = eligiblePlayer();
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertFalse(behavior.onKeyword(context, "rowan"));
  }
}
