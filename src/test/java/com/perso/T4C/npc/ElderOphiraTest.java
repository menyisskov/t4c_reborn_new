package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestService;
import com.perso.T4C.quest.definition.AvalonWildsVigil;
import com.perso.T4C.quest.definition.FadingVeilReckoning;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ElderOphiraTest {
  private static final QuestDef WILDS = AvalonWildsVigil.definition();
  private static final QuestDef VEIL = FadingVeilReckoning.definition();

  private final AtomicInteger saves = new AtomicInteger();
  private final QuestService quests =
      new QuestService(XpCurve.loadDefault(), saves::incrementAndGet, null);

  private ElderOphira npc() throws Exception {
    return new ElderOphira(new NpcContext(quests));
  }

  private static void keyword(ElderOphira npc, Player player, String text) {
    assertTrue(npc.publicBehavior().onKeyword(new NpcBehaviorContext(npc, player), text));
  }

  private static void greet(ElderOphira npc, Player player) throws Exception {
    var start = ScriptedNpc.class.getDeclaredMethod("onInteractStart", Player.class);
    start.setAccessible(true);
    start.invoke(npc, player);
  }

  private static void ready(Player player, QuestDef quest) {
    player.setQuestFlag(QuestService.statusFlag(quest), QuestService.STATUS_ACTIVE);
    player.setQuestFlag(QuestService.killsFlag(quest), quest.getRequiredKills());
    player.getInventory().add(quest.getRequiredItemKey());
  }

  @Test
  void mustHearAnOfferAndExplicitlyAcceptAndCannotSkipToVeil() throws Exception {
    ElderOphira npc = npc();
    Player player = new Player();
    greet(npc, player);
    keyword(npc, player, "accept");
    keyword(npc, player, "report");
    keyword(npc, player, "veil");
    keyword(npc, player, "accept");
    assertEquals(QuestService.STATUS_NOT_STARTED, QuestService.statusFor(player, WILDS));
    assertEquals(QuestService.STATUS_NOT_STARTED, QuestService.statusFor(player, VEIL));
    assertEquals(0, saves.get());

    keyword(npc, player, "wilds");
    assertEquals(QuestService.STATUS_NOT_STARTED, QuestService.statusFor(player, WILDS));
    keyword(npc, player, "accept");
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, WILDS));
    assertEquals(1, saves.get());
    keyword(npc, player, "veil");
    keyword(npc, player, "accept");
    assertEquals(QuestService.STATUS_NOT_STARTED, QuestService.statusFor(player, VEIL));
  }

  @Test
  void greetingAndLoreNeverTurnInEvenWhenBothObjectivesAreReady() throws Exception {
    ElderOphira npc = npc();
    Player player = new Player();
    ready(player, WILDS);
    int gold = player.getGold();
    greet(npc, player);
    keyword(npc, player, "wilds");
    keyword(npc, player, "accept");
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, WILDS));
    assertTrue(player.getInventory().contains(WILDS.getRequiredItemKey()));
    assertEquals(gold, player.getGold());
    assertEquals(0, saves.get());
  }

  @Test
  void bothKillAndItemObjectivesMustBeMetBeforeReportUnlocksNextChapter() throws Exception {
    ElderOphira npc = npc();
    Player player = new Player();
    keyword(npc, player, "wilds");
    keyword(npc, player, "accept");
    player.getInventory().add(WILDS.getRequiredItemKey());
    keyword(npc, player, "report");
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, WILDS));
    assertTrue(player.getInventory().remove(WILDS.getRequiredItemKey()));
    for (int i = 0; i < WILDS.getRequiredKills(); i++) {
      quests.recordKill(player, "Moonlit Stalker", 0, 1265, 1400);
    }
    keyword(npc, player, "report");
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, WILDS));
    keyword(npc, player, "veil");
    keyword(npc, player, "accept");
    assertEquals(QuestService.STATUS_NOT_STARTED, QuestService.statusFor(player, VEIL));

    player.getInventory().add(WILDS.getRequiredItemKey());
    int gold = player.getGold();
    keyword(npc, player, "report");
    assertEquals(QuestService.STATUS_COMPLETED, QuestService.statusFor(player, WILDS));
    assertFalse(player.getInventory().contains(WILDS.getRequiredItemKey()));
    assertEquals(gold + WILDS.getRewardGold(), player.getGold());
    assertTrue(QuestService.hasUnlockedZone(player, "avalon_wilds"));
    keyword(npc, player, "veil");
    assertEquals(QuestService.STATUS_NOT_STARTED, QuestService.statusFor(player, VEIL));
    keyword(npc, player, "accept");
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, VEIL));
  }

  @Test
  void oldProgressAndCompletedFlagsSurviveSaveLoadWithoutDuplicateRewards() throws Exception {
    Player old = new Player();
    old.setQuestFlag(QuestService.statusFlag(WILDS), QuestService.STATUS_ACTIVE);
    old.setQuestFlag(QuestService.killsFlag(WILDS), 19);
    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(old), restored);
    quests.recordKill(restored, "Moonlit Stalker", 0, 1265, 1400);
    assertEquals(20, restored.getQuestFlag(QuestService.killsFlag(WILDS)));
    restored.getInventory().add(WILDS.getRequiredItemKey());
    ElderOphira npc = npc();
    keyword(npc, restored, "report");
    int gold = restored.getGold();
    Player reloaded = new Player();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(restored), reloaded);
    ElderOphira freshNpc = npc();
    greet(freshNpc, reloaded);
    keyword(freshNpc, reloaded, "wilds");
    keyword(freshNpc, reloaded, "report");
    keyword(freshNpc, reloaded, "report");
    assertEquals(gold, reloaded.getGold());
    assertEquals(QuestService.STATUS_COMPLETED, QuestService.statusFor(reloaded, WILDS));
    assertTrue(QuestService.hasUnlockedZone(reloaded, "avalon_wilds"));
  }

  @Test
  void oldIndependentlyAcceptedVeilRemainsCompletableAndReportsOnlySelectedQuest()
      throws Exception {
    Player player = new Player();
    ready(player, VEIL);
    ElderOphira npc = npc();
    greet(npc, player);
    keyword(npc, player, "veil");
    int gold = player.getGold();
    keyword(npc, player, "report");
    assertEquals(QuestService.STATUS_COMPLETED, QuestService.statusFor(player, VEIL));
    assertEquals(QuestService.STATUS_NOT_STARTED, QuestService.statusFor(player, WILDS));
    assertEquals(gold + VEIL.getRewardGold(), player.getGold());

    Player both = new Player();
    ready(both, WILDS);
    ready(both, VEIL);
    greet(npc, both);
    keyword(npc, both, "wilds");
    keyword(npc, both, "report");
    assertEquals(QuestService.STATUS_COMPLETED, QuestService.statusFor(both, WILDS));
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(both, VEIL));
    assertTrue(both.getInventory().contains(VEIL.getRequiredItemKey()));
  }

  @Test
  void offersDoNotLeakAcrossConversationsOrCharacters() throws Exception {
    ElderOphira npc = npc();
    Player player = new Player();
    keyword(npc, player, "wilds");
    greet(npc, player);
    keyword(npc, player, "accept");
    assertEquals(QuestService.STATUS_NOT_STARTED, QuestService.statusFor(player, WILDS));
    keyword(npc, player, "wilds");
    Player other = new Player();
    keyword(npc, other, "accept");
    assertEquals(QuestService.STATUS_NOT_STARTED, QuestService.statusFor(other, WILDS));
  }

  @Test
  void dialogueRegistersClickableAcceptanceReportingAndRouteKeywords() throws Exception {
    var method = ScriptedNpc.class.getDeclaredMethod("getDialogKeywords");
    method.setAccessible(true);
    var keywords = (java.util.List<?>) method.invoke(npc());
    assertTrue(
        keywords.containsAll(java.util.List.of("wilds", "veil", "accept", "report", "route")));
  }
}
