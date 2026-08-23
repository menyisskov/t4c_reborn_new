package com.perso.T4C.npc;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class CustodianTest {

  private static NpcBehaviorContext context(Player player) throws Exception {
    return new NpcBehaviorContext(new Custodian(new NpcContext(null)), player);
  }

  @Test
  void storylineStep34RequiresAskingAboutTheRiteBeforeTheQuiz() throws Exception {
    Player player = new Player();
    player.setQuestFlag("ADDON_STORYLINE_PROGRESS", 34);
    NpcBehaviorContext context = context(player);
    NpcBehavior behavior = context.npc().publicBehavior();

    behavior.onConversationStart(context);
    assertNull(context.pendingYesNo());

    assertTrue(behavior.onKeyword(context, "rite of passage"));
    assertEquals(35, player.getQuestFlag("ADDON_STORYLINE_PROGRESS"));
  }

  @Test
  void correctRiteAnswersGrantLibraryAccess() throws Exception {
    Player player = new Player();
    player.setQuestFlag("ADDON_STORYLINE_PROGRESS", 35);
    NpcBehaviorContext context = context(player);
    NpcBehavior behavior = context.npc().publicBehavior();

    behavior.onConversationStart(context);
    assertEquals("READY", context.pendingYesNo());
    assertTrue(behavior.onYesNo(context, "READY", true));
    assertTrue(behavior.onKeyword(context, "to look upon one's self"));
    assertTrue(behavior.onKeyword(context, "one must only have eyes for knowledge"));
    assertTrue(behavior.onKeyword(context, "to recognize one's own ignorance"));
    assertTrue(behavior.onKeyword(context, "one must open these eyes to the world"));

    assertEquals(1, player.getQuestFlag("ADDON_CUSTODIAN_ACCESS"));
  }

  @Test
  void accessConfirmationTeleportsToTheFourthFloor() throws Exception {
    Player player = new Player();
    player.setQuestFlag("ADDON_STORYLINE_PROGRESS", 36);
    player.setQuestFlag("ADDON_CUSTODIAN_ACCESS", 1);
    NpcBehaviorContext context = context(player);
    NpcBehavior behavior = context.npc().publicBehavior();

    behavior.onConversationStart(context);
    assertEquals("GO_UP", context.pendingYesNo());
    assertTrue(behavior.onYesNo(context, "GO_UP", true));
    assertEquals(1081 * GRID_W, player.getCoordinates().getX());
    assertEquals(1465 * GRID_H, player.getCoordinates().getY());
  }
}
