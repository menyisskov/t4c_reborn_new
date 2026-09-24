package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.quest.QuestService;
import org.junit.jupiter.api.Test;

/** T4C-0032: the "avalon"/"passage" keyword must dispatch to whichever stage of the
 * tideworn_shore_scouts -> passage_to_avalon chain the player is actually on. */
class HarbormasterRangorTest {
  @Test
  void offersTheScoutingStageFirstThenPassageOnceScoutsAreDone() throws Exception {
    QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);
    HarbormasterRangor npc = new HarbormasterRangor(new NpcContext(quests));
    Player player = new Player();
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onKeyword(context, "avalon"));

    QuestDef scouts = QuestRegistry.findById("tideworn_shore_scouts");
    assertNotNull(scouts);
    assertEquals(
        QuestService.STATUS_ACTIVE, player.getQuestFlag(QuestService.statusFlag(scouts)));

    QuestDef passage = QuestRegistry.findById("passage_to_avalon");
    assertNotNull(passage);
    assertEquals(
        QuestService.STATUS_NOT_STARTED, player.getQuestFlag(QuestService.statusFlag(passage)));

    player.setQuestFlag(QuestService.killsFlag(scouts), scouts.getRequiredKills());
    assertNotNull(quests.turnInReadyQuests(HarbormasterRangor.ID, player));
    assertEquals(
        QuestService.STATUS_COMPLETED, player.getQuestFlag(QuestService.statusFlag(scouts)));

    assertTrue(behavior.onKeyword(context, "passage"));
    assertEquals(
        QuestService.STATUS_ACTIVE, player.getQuestFlag(QuestService.statusFlag(passage)));
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
    assertTrue(behavior.onKeyword(context, "avalon"));

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
