package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class QuestServiceTest {
  private static final QuestDef QUEST =
      new QuestDef(
          "lighthaven_samaritan_rats",
          "Les rats du sous-sol du temple",
          "LighthavenSamaritan",
          "Brown Rat",
          15,
          1,
          304,
          383,
          120,
          500,
          300,
          "Offre",
          "Réussite",
          "Déjà terminée");

  @Test
  void legacyNewbieMarkerActivatesTheRatQuestAndTracksProgress() {
    Player player = new Player();
    player.setQuestFlag("__NEWBIE_QUEST", 1);
    QuestDef flaggedQuest =
        new QuestDef(
            QUEST.getId(),
            QUEST.getTitle(),
            QUEST.getGiverNpc(),
            QUEST.getTargetMonster(),
            QUEST.getRequiredKills(),
            QUEST.getTargetWorldZ(),
            QUEST.getAreaCenterX(),
            QUEST.getAreaCenterY(),
            QUEST.getAreaRadiusTiles(),
            QUEST.getRewardGold(),
            QUEST.getRewardXp(),
            QUEST.getOfferText(),
            QUEST.getCompletionText(),
            QUEST.getCompletedText(),
            "__NEWBIE_QUEST");
    QuestService service =
        new QuestService(XpCurve.loadDefault(), null, null, () -> List.of(flaggedQuest));
    assertEquals(QuestService.STATUS_ACTIVE, QuestService.statusFor(player, flaggedQuest));
    assertEquals(
        QuestService.STATUS_NOT_STARTED,
        player.getQuestFlag(QuestService.statusFlag(flaggedQuest)));
    assertTrue(service.recordKill(player, "Brown Rat", 1, 304, 383));
    assertEquals(1, player.getQuestFlag(QuestService.killsFlag(flaggedQuest)));
    assertEquals(1, player.getQuestFlag("__NEWBIE_QUEST"));
  }

  @Test
  void oneTimeQuestPersistsProgressAndPaysExactRewardOnlyAtTurnIn() throws Exception {
    AtomicInteger saves = new AtomicInteger();
    List<String> messages = new ArrayList<>();
    QuestService service =
        new QuestService(
            XpCurve.loadDefault(), saves::incrementAndGet, messages::add, () -> List.of(QUEST));
    Player player = new Player();
    player.setLevel(1);
    player.setXpToNextLevel(100);
    assertEquals("Offre", service.giveOrReport(QUEST.getId(), QUEST.getGiverNpc(), player));
    assertEquals(QuestService.STATUS_ACTIVE, player.getQuestFlag(QuestService.statusFlag(QUEST)));
    assertEquals(0, player.getQuestFlag(QuestService.killsFlag(QUEST)));
    assertEquals(1, saves.get());
    assertTrue(service.giveOrReport(QUEST.getId(), QUEST.getGiverNpc(), player).contains("0/15"));
    assertEquals(1, saves.get(), "asking for work again must not restart or save the quest");
    assertNull(service.turnInReadyQuests(QUEST.getGiverNpc(), player));
    assertEquals(0, player.getGold());
    assertFalse(service.recordKill(null, "Brown Rat", 1, 304, 383));
    assertFalse(service.recordKill(player, "Goblin", 1, 304, 383));
    assertFalse(service.recordKill(player, "Brown Rat", 0, 304, 383));
    assertFalse(service.recordKill(player, "Brown Rat", 1, 425, 383));
    assertEquals(1, saves.get(), "rejected kills must not be persisted");
    assertTrue(
        service.recordKill(player, "Rat", 1, 304, 383),
        "the canonical Rat alias must count as Brown Rat");
    assertEquals(1, player.getQuestFlag(QuestService.killsFlag(QUEST)));
    assertEquals(2, saves.get());
    PlayerStateDto savedState = PlayerStateMapper.fromPlayer(player);
    Player restored = new Player();
    PlayerStateMapper.applyToPlayer(savedState, restored);
    assertEquals(QuestService.STATUS_ACTIVE, restored.getQuestFlag(QuestService.statusFlag(QUEST)));
    assertEquals(1, restored.getQuestFlag(QuestService.killsFlag(QUEST)));
    for (int i = 1; i < QUEST.getRequiredKills(); i++) {
      assertTrue(service.recordKill(restored, "Brown Rat", 1, 304, 383));
    }
    assertEquals(15, restored.getQuestFlag(QuestService.killsFlag(QUEST)));
    assertEquals(QuestService.STATUS_ACTIVE, restored.getQuestFlag(QuestService.statusFlag(QUEST)));
    assertEquals(0, restored.getGold(), "the fifteenth kill must not pay the reward");
    assertEquals(0, restored.getCurrentXp(), "the fifteenth kill must not pay XP");
    assertTrue(messages.get(messages.size() - 1).contains("Return to see"));
    assertTrue(
        service.giveOrReport(QUEST.getId(), QUEST.getGiverNpc(), restored).contains("15/15"));
    assertEquals(QuestService.STATUS_ACTIVE, restored.getQuestFlag(QuestService.statusFlag(QUEST)));
    assertEquals(0, restored.getGold(), "GIVE_QUEST itself must not turn in a ready quest");
    assertNull(service.turnInReadyQuests("AnotherNpc", restored));
    restored.applyBuff(
        "Double XP",
        "",
        "",
        null,
        true,
        List.of(new SpellData.SpellEffect("ATTRIBUTE", "exp", "100", "")));
    assertEquals(1f, restored.getBuffXpMultiplier());
    assertEquals("Réussite", service.turnInReadyQuests(QUEST.getGiverNpc(), restored));
    assertEquals(
        QuestService.STATUS_COMPLETED, restored.getQuestFlag(QuestService.statusFlag(QUEST)));
    assertEquals(15, restored.getQuestFlag(QuestService.killsFlag(QUEST)));
    assertEquals(500, restored.getGold());
    assertEquals(4, restored.getLevel());
    assertEquals(
        277,
        restored.getCurrentXp(),
        "300 reward XP scaled 5x by SERVER_XP_RATE (1500) spends 100+360+763 across three"
            + " level-ups and leaves 277");
    assertEquals(15, restored.getStatPoints(), "5 stat points per level-up, three level-ups");
    assertEquals(45, restored.getSkillPoints(), "15 skill points per level-up, three level-ups");
    assertEquals(17, saves.get(), "acceptance, fifteen kills and turn-in must each persist");
    assertNull(service.turnInReadyQuests(QUEST.getGiverNpc(), restored));
    assertEquals(
        "Déjà terminée", service.giveOrReport(QUEST.getId(), QUEST.getGiverNpc(), restored));
    assertFalse(service.recordKill(restored, "Brown Rat", 1, 304, 383));
    assertEquals(500, restored.getGold());
    assertEquals(4, restored.getLevel());
    assertEquals(277, restored.getCurrentXp());
    assertEquals(17, saves.get(), "a completed one-time quest must never save or pay again");
  }
}
