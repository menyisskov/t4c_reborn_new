package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.FadingVeilReckoning;
import com.perso.T4C.quest.definition.HollowDawnCampaign;
import com.perso.T4C.teleport.NamedLocations;
import org.junit.jupiter.api.Test;

class HollowDawnDialogueTest {
  private final QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);

  @Test
  void maelinRequiresTheVeilAndAdvancesOnlyAfterReportAndLevelGate() throws Exception {
    ChroniclerMaelin maelin = new ChroniclerMaelin(new NpcContext(quests));
    Player player = new Player();
    QuestDef first = HollowDawnCampaign.avalon().getFirst();
    say(maelin, player, "accept");
    assertEquals(QuestService.STATUS_NOT_STARTED, QuestService.statusFor(player, first));

    completeFlag(player, FadingVeilReckoning.definition());
    say(maelin, player, "story");
    say(maelin, player, "moonwake");
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, first));
    for (int i = 0; i < first.getRequiredKills(); i++)
      quests.recordKill(
          player, first.getTargetMonster(), 0, first.getAreaCenterX(), first.getAreaCenterY());
    say(maelin, player, "report");
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, first));
    player.setLevel(first.getMinLevel());
    say(maelin, player, "report");
    assertEquals(QuestService.STATUS_COMPLETED, QuestService.statusFor(player, first));
    hearClue(player, "moonwake");
    say(maelin, player, "accept");
    assertEquals(
        QuestService.STATUS_ACTIVE,
        QuestService.statusFor(player, HollowDawnCampaign.avalon().get(1)));
  }

  @Test
  void keeperAndTravelOpenOnlyAfterMaelinsFinalAccount() throws Exception {
    ChroniclerMaelin maelin = new ChroniclerMaelin(new NpcContext(quests));
    KeeperVael keeper = new KeeperVael(new NpcContext(quests));
    Player player = new Player();
    completeFlag(player, FadingVeilReckoning.definition());
    completeFlag(player, HollowDawnCampaign.avalon().get(2));
    hearClue(player, "emberglass");
    QuestDef marshal = HollowDawnCampaign.avalon().getLast();
    say(keeper, player, "accept");
    assertEquals(
        QuestService.STATUS_NOT_STARTED,
        QuestService.statusFor(player, HollowDawnCampaign.threnody().getFirst()));
    assertFalse(has(player, "Threnody Reach"));

    say(maelin, player, "accept");
    quests.recordKill(
        player, marshal.getTargetMonster(), 0, marshal.getAreaCenterX(), marshal.getAreaCenterY());
    player.setLevel(299);
    say(maelin, player, "report");
    assertFalse(has(player, "Threnody Reach"));
    player.setLevel(300);
    say(maelin, player, "report");
    assertTrue(has(player, "Threnody Reach"));
    assertFalse(has(player, "The Hollow Dawn"));
    say(keeper, player, "accept");
    assertEquals(
        QuestService.STATUS_ACTIVE,
        QuestService.statusFor(player, HollowDawnCampaign.threnody().getFirst()));
  }

  @Test
  void lieutenantReportsRequireTheirGuaranteedRelics() throws Exception {
    Player player = new Player();
    player.setLevel(400);
    completeFlag(player, FadingVeilReckoning.definition());
    completeFlag(player, HollowDawnCampaign.avalon().getFirst());
    ChroniclerMaelin maelin = new ChroniclerMaelin(new NpcContext(quests));
    hearClue(player, "moonwake");
    QuestDef cantor = HollowDawnCampaign.avalon().get(1);
    assertRelicStage(maelin, player, cantor);

    completeFlag(player, HollowDawnCampaign.avalon().get(2));
    completeFlag(player, HollowDawnCampaign.avalon().get(3));
    for (int i = 0; i < 3; i++) completeFlag(player, HollowDawnCampaign.threnody().get(i));
    KeeperVael keeper = new KeeperVael(new NpcContext(quests));
    assertRelicStage(keeper, player, HollowDawnCampaign.threnody().get(3));
  }

  @Test
  void eitherChosenPathCanReachVaelWithoutCompletingTheOther() throws Exception {
    for (String choice : new String[] {"moonwake", "emberglass"}) {
      Player player = new Player();
      player.setLevel(400);
      completeFlag(player, FadingVeilReckoning.definition());
      ChroniclerMaelin maelin = new ChroniclerMaelin(new NpcContext(quests));
      say(maelin, player, choice);
      var path = HollowDawnCampaign.selectedPath(player);
      assertEquals(2, path.size());
      say(maelin, player, choice.equals("moonwake") ? "emberglass" : "moonwake");
      assertEquals(
          path.stream().map(QuestDef::getId).toList(),
          HollowDawnCampaign.selectedPath(player).stream().map(QuestDef::getId).toList());
      for (QuestDef stage : path) {
        if (stage != path.getFirst()) hearClue(player, choice);
        say(maelin, player, "accept");
        for (int i = 0; i < stage.getRequiredKills(); i++) {
          quests.recordKill(
              player, stage.getTargetMonster(), 0, stage.getAreaCenterX(), stage.getAreaCenterY());
        }
        if (stage.getRequiredItemKey() != null)
          player.getInventory().add(stage.getRequiredItemKey());
        say(maelin, player, "report");
        assertEquals(QuestService.STATUS_COMPLETED, QuestService.statusFor(player, stage));
      }
      assertTrue(has(player, "Threnody Reach"));
      for (QuestDef other : HollowDawnCampaign.avalon()) {
        if (path.stream().noneMatch(q -> q.getId().equals(other.getId())))
          assertEquals(0, QuestService.statusFor(player, other));
      }
      KeeperVael keeper = new KeeperVael(new NpcContext(quests));
      say(keeper, player, "accept");
      assertEquals(
          QuestService.STATUS_ACTIVE,
          QuestService.statusFor(player, HollowDawnCampaign.threnody().getFirst()));
    }
  }

  @Test
  void oldLinearSavesContinueActiveWorkAndRecoverTheEarlierBellPassage() throws Exception {
    Player player = new Player();
    completeFlag(player, HollowDawnCampaign.avalon().get(1));
    QuestDef oldActive = HollowDawnCampaign.avalon().get(2);
    player.setQuestFlag(QuestService.statusFlag(oldActive), QuestService.STATUS_ACTIVE);
    ChroniclerMaelin maelin = new ChroniclerMaelin(new NpcContext(quests));
    maelin.publicBehavior().onConversationStart(new NpcBehaviorContext(maelin, player));
    assertTrue(has(player, "Threnody Reach"));
    say(maelin, player, "accept");
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, oldActive));
    assertEquals(oldActive.getId(), HollowDawnCampaign.selectedPath(player).getFirst().getId());
    assertEquals(0, QuestService.statusFor(player, HollowDawnCampaign.avalon().getFirst()));
  }

  @Test
  void legacyActiveThrenodyQuestDoesNotRequireNewPrerequisiteFlags() throws Exception {
    Player player = new Player();
    QuestDef active = HollowDawnCampaign.threnody().get(2);
    player.setQuestFlag(QuestService.statusFlag(active), QuestService.STATUS_ACTIVE);
    KeeperVael keeper = new KeeperVael(new NpcContext(quests));
    say(keeper, player, "accept");
    assertEquals(0, QuestService.statusFor(player, HollowDawnCampaign.threnody().getFirst()));
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, active));
  }

  private static void hearClue(Player player, String branch) throws Exception {
    CampaignWitnessNpc witness =
        branch.equals("moonwake")
            ? new MoonwakeWitnessIlyra(new NpcContext(null))
            : new EmberglassWarderSoren(new NpcContext(null));
    assertTrue(
        witness.publicBehavior().onKeyword(new NpcBehaviorContext(witness, player), "testimony"));
    assertTrue(witness.publicBehavior().onKeyword(new NpcBehaviorContext(witness, player), "clue"));
  }

  @Test
  void advertisedCampaignKeywordsRemainConnectedThroughTheFinalEpilogue() throws Exception {
    Player player = new Player();
    completeFlag(player, FadingVeilReckoning.definition());
    ChroniclerMaelin maelin = new ChroniclerMaelin(new NpcContext(quests));
    for (String keyword :
        new String[] {
          "story",
          "accept",
          "report",
          "route",
          "witnesses",
          "warders",
          "moonwake",
          "emberglass",
          "hierarchy",
          "threnody",
          "epilogue"
        }) {
      say(maelin, player, keyword);
    }

    completeFlag(player, HollowDawnCampaign.avalon().get(1));
    KeeperVael keeper = new KeeperVael(new NpcContext(quests));
    for (String keyword :
        new String[] {
          "story",
          "accept",
          "report",
          "route",
          "witnesses",
          "warders",
          "moonwake",
          "emberglass",
          "hierarchy",
          "threnody",
          "epilogue"
        }) {
      say(keeper, player, keyword);
    }
    completeFlag(player, HollowDawnCampaign.threnody().getLast());
    say(keeper, player, "epilogue");
  }

  @Test
  void everyChapterCueAndHandoffResolvesToPlayerFacingText() {
    for (QuestDef quest : HollowDawnCampaign.all()) {
      assertTrue(I18n.has(I18n.keyOf(HollowDawnCampaign.chapterCue(quest))), quest.getId());
      assertTrue(I18n.has(I18n.keyOf(HollowDawnCampaign.chapterHandoff(quest))), quest.getId());
    }
    for (var topic : CampaignTopics.all()) {
      assertTrue(I18n.has(I18n.keyOf(topic.response())));
      for (String keyword : topic.keywords()) assertTrue(I18n.has(I18n.keyOf(keyword)));
    }
  }

  private void assertRelicStage(CampaignQuestNpc npc, Player player, QuestDef stage) {
    assertTrue(stage.getRequiredItemKey().startsWith("item."));
    assertTrue(ItemRegistry.findByKey(stage.getRequiredItemKey()) != null);
    assertTrue(
        MonsterRegistry.findByName(stage.getTargetMonster()).getLoot().stream()
            .anyMatch(
                drop ->
                    stage.getRequiredItemKey().equals(drop.getItem()) && drop.getChance() >= 1f));
    say(npc, player, "accept");
    quests.recordKill(
        player, stage.getTargetMonster(), 0, stage.getAreaCenterX(), stage.getAreaCenterY());
    say(npc, player, "report");
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, stage));
    player.getInventory().add(stage.getRequiredItemKey());
    say(npc, player, "report");
    assertEquals(QuestService.STATUS_COMPLETED, QuestService.statusFor(player, stage));
    assertFalse(player.getInventory().contains(stage.getRequiredItemKey()));
  }

  private static void say(CampaignQuestNpc npc, Player player, String keyword) {
    assertTrue(npc.publicBehavior().onKeyword(new NpcBehaviorContext(npc, player), keyword));
  }

  private static void completeFlag(Player player, QuestDef quest) {
    player.setQuestFlag(QuestService.statusFlag(quest), QuestService.STATUS_COMPLETED);
  }

  private static boolean has(Player player, String name) {
    return NamedLocations.forPlayer(player).stream().anyMatch(l -> name.equals(l.displayName()));
  }
}
