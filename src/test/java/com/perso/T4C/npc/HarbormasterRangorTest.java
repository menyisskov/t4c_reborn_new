package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.RebirthBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.teleport.NamedLocations;
import org.junit.jupiter.api.Test;

/** The Stoneheim crossing is conversational, ordered, and permanently unlocked. */
class HarbormasterRangorTest {
  @Test
  void offersTheScoutingStageFirstThenPassageOnceScoutsAreDone() throws Exception {
    QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);
    HarbormasterRangor npc = new HarbormasterRangor(new NpcContext(quests));
    Player player = new Player();
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onKeyword(context, "isles"));

    QuestDef scouts = QuestRegistry.findById("tideworn_shore_scouts");
    assertNotNull(scouts);
    assertEquals(QuestService.STATUS_NOT_STARTED, QuestService.statusFor(player, scouts));
    assertTrue(behavior.onKeyword(context, "scouts"));
    assertEquals(QuestService.STATUS_ACTIVE, player.getQuestFlag(QuestService.statusFlag(scouts)));

    QuestDef passage = QuestRegistry.findById("passage_to_avalon");
    assertNotNull(passage);
    assertEquals(
        QuestService.STATUS_NOT_STARTED, player.getQuestFlag(QuestService.statusFlag(passage)));

    player.setQuestFlag(QuestService.killsFlag(scouts), scouts.getRequiredKills());
    player.setLevel(199);
    assertTrue(behavior.onKeyword(context, "report"));
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, scouts));
    player.setLevel(200);
    assertTrue(behavior.onKeyword(context, "report"));
    assertEquals(
        QuestService.STATUS_COMPLETED, player.getQuestFlag(QuestService.statusFlag(scouts)));

    assertTrue(behavior.onKeyword(context, "chart"));
    assertEquals(QuestService.STATUS_ACTIVE, player.getQuestFlag(QuestService.statusFlag(passage)));
    player.setQuestFlag(QuestService.killsFlag(passage), passage.getRequiredKills());
    assertTrue(behavior.onKeyword(context, "report"));
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, passage));
    player.getInventory().add("item.tideworn_avalon_chart");
    assertTrue(behavior.onKeyword(context, "report"));
    assertEquals(QuestService.STATUS_COMPLETED, QuestService.statusFor(player, passage));
    assertFalse(player.getInventory().contains("item.tideworn_avalon_chart"));
    assertTrue(
        NamedLocations.forPlayer(player).stream()
            .anyMatch(loc -> "Witness Isles Sanctuary".equals(loc.displayName())));
    assertTrue(RebirthBehavior.perform(player));
    assertTrue(
        NamedLocations.forPlayer(player).stream()
            .anyMatch(loc -> "Witness Isles Sanctuary".equals(loc.displayName())));
  }

  @Test
  void aCharacterAlreadyDoneWithTheOriginalSingleStageQuestIsNeverOfferedScoutsAgain()
      throws Exception {
    QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);
    HarbormasterRangor npc = new HarbormasterRangor(new NpcContext(quests));
    Player player = new Player();
    QuestDef passage = QuestRegistry.findById("passage_to_avalon");
    assertNotNull(passage);
    player.setQuestFlag(QuestService.statusFlag(passage), QuestService.STATUS_COMPLETED);

    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);
    assertTrue(behavior.onKeyword(context, "isles"));

    QuestDef scouts = QuestRegistry.findById("tideworn_shore_scouts");
    assertNotNull(scouts);
    assertEquals(
        QuestService.STATUS_NOT_STARTED, player.getQuestFlag(QuestService.statusFlag(scouts)));
  }

  @Test
  void unrelatedKeywordsFallThroughToTheDeclarativeTopics() throws Exception {
    QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);
    HarbormasterRangor npc = new HarbormasterRangor(new NpcContext(quests));
    Player player = new Player();
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertEquals(false, behavior.onKeyword(context, "ithrak"));
  }
}
