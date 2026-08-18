package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.npc.behavior.*;
import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.stoneheim.Oracle;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class OracleRebirthTest {
  private static NpcBehaviorContext context(Player player) throws Exception {
    return new NpcBehaviorContext(new Oracle(new NpcContext(null)), player);
  }

  private static Player eligiblePlayer() {
    Player player = new Player();
    player.setLevel(100);
    player.setQuestFlag("__FLAG_USER_HAS_DEFEATED_ASSISTANT", 1);
    return player;
  }

  @Test
  void OracleHasNoLegacyTextDefinition() {
    assertNull(Oracle.spec().sourceScript());
    assertTrue(Oracle.spec().sourceEvents().isEmpty());
  }

  @Test
  void offersRebirthConfirmationToAnEligiblePlayer() throws Exception {
    Player player = eligiblePlayer();
    NpcBehaviorContext context = context(player);
    Oracle.behavior().onConversationStart(context);
    Oracle.behavior().onKeyword(context, "ready reborn");
    assertEquals("REBIRTH", context.pendingYesNo());
  }

  @Test
  void refusesRebirthBelowTheRequiredLevel() throws Exception {
    Player player = new Player();
    player.setLevel(74);
    player.setQuestFlag("__FLAG_USER_HAS_DEFEATED_ASSISTANT", 1);
    NpcBehaviorContext context = context(player);
    Oracle.behavior().onKeyword(context, "ready reborn");
    assertNull(context.pendingYesNo());
  }

  @Test
  void confirmingRebirthTeleportsAndIncrementsTheCounter() throws Exception {
    Player player = eligiblePlayer();
    NpcBehaviorContext context = context(player);
    Oracle.behavior().onConversationStart(context);
    assertTrue(Oracle.behavior().onYesNo(context, "REBIRTH", true));
    assertEquals(1, player.getRebirthCount());
    assertEquals(1315 * com.perso.T4C.config.GameConstants.GRID_W, player.getCoordinates().getX());
    assertEquals(920 * com.perso.T4C.config.GameConstants.GRID_H, player.getCoordinates().getY());
    assertEquals(1, player.getCoordinates().getZ());
  }

  @Test
  void rebirthDemotesAndGrantsSeraphRegalia() throws Exception {
    Player player = eligiblePlayer();
    player.setStrength(500);
    player.setCurrentXp(98791);
    NpcBehaviorContext context = context(player);
    Oracle.behavior().onConversationStart(context);
    Oracle.behavior().onYesNo(context, "REBIRTH", true);
    assertEquals(1, player.getLevel());
    assertEquals(0, player.getCurrentXp());
    assertEquals(
        "item.remort_white_wings",
        player.getEquippedItems().get(com.perso.T4C.player.BodyPart.BACK));
    assertEquals(
        "item.ring_of_the_seraph",
        player.getEquippedItems().get(com.perso.T4C.player.BodyPart.RING1));
  }

  @Test
  void readyRebornKeywordIsHandledByJavaBehaviour() throws Exception {
    Player player = eligiblePlayer();
    NpcBehaviorContext context = context(player);
    assertTrue(Oracle.behavior().onKeyword(context, "ready reborn"));
    assertEquals("REBIRTH", context.pendingYesNo());
  }
}
