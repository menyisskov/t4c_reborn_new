package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.XpCurve;
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
    say(maelin, player, "accept");
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, first));
    for (int i = 0; i < first.getRequiredKills(); i++)
      quests.recordKill(
          player, first.getTargetMonster(), 0, first.getAreaCenterX(), first.getAreaCenterY());
    say(maelin, player, "report");
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, first));
    player.setLevel(first.getMinLevel());
    say(maelin, player, "report");
    assertEquals(QuestService.STATUS_COMPLETED, QuestService.statusFor(player, first));
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
    for (int i = 0; i < 3; i++) completeFlag(player, HollowDawnCampaign.avalon().get(i));
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
    QuestDef cantor = HollowDawnCampaign.avalon().get(1);
    assertRelicStage(maelin, player, cantor);

    completeFlag(player, HollowDawnCampaign.avalon().get(2));
    completeFlag(player, HollowDawnCampaign.avalon().get(3));
    for (int i = 0; i < 3; i++) completeFlag(player, HollowDawnCampaign.threnody().get(i));
    KeeperVael keeper = new KeeperVael(new NpcContext(quests));
    assertRelicStage(keeper, player, HollowDawnCampaign.threnody().get(3));
  }

  private void assertRelicStage(CampaignQuestNpc npc, Player player, QuestDef stage) {
    assertTrue(stage.getRequiredItemKey().startsWith("item."));
    assertTrue(ItemRegistry.findByKey(stage.getRequiredItemKey()) != null);
    assertTrue(MonsterRegistry.findByName(stage.getTargetMonster()).getLoot().stream()
        .anyMatch(drop -> stage.getRequiredItemKey().equals(drop.getItem())
            && drop.getChance() >= 1f));
    say(npc, player, "accept");
    quests.recordKill(player, stage.getTargetMonster(), 0,
        stage.getAreaCenterX(), stage.getAreaCenterY());
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
