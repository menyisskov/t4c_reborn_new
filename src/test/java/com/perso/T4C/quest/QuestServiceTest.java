package com.perso.T4C.quest;

import com.perso.T4C.helper.PlayerStateDto;
import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestServiceTest {
    private static final QuestDef QUEST = new QuestDef(
            "lighthaven_samaritan_rats",
            "Les rats du sous-sol du temple",
            "LighthavenSamaritan",
            "Brown Rat",
            10,
            1,
            304,
            383,
            120,
            500,
            300,
            "Offre",
            "Réussite",
            "Déjà terminée"
    );

    @Test
    void oneTimeQuestPersistsProgressAndPaysExactRewardOnlyAtTurnIn() throws Exception {
        AtomicInteger saves = new AtomicInteger();
        List<String> messages = new ArrayList<>();
        QuestService service = new QuestService(
                XpCurve.loadDefault(), saves::incrementAndGet, messages::add, () -> List.of(QUEST));
        Player player = new Player();
        player.setLevel(1);
        player.setXpToNextLevel(100);

        assertEquals("Offre", service.giveOrReport(QUEST.getId(), QUEST.getGiverNpc(), player));
        assertEquals(QuestService.STATUS_ACTIVE,
                player.getQuestFlag(QuestService.statusFlag(QUEST)));
        assertEquals(0, player.getQuestFlag(QuestService.killsFlag(QUEST)));
        assertEquals(1, saves.get());

        assertTrue(service.giveOrReport(QUEST.getId(), QUEST.getGiverNpc(), player)
                .contains("0/10"));
        assertEquals(1, saves.get(), "asking for work again must not restart or save the quest");
        assertNull(service.turnInReadyQuests(QUEST.getGiverNpc(), player));
        assertEquals(0, player.getGold());

        assertFalse(service.recordKill(null, "Brown Rat", 1, 304, 383));
        assertFalse(service.recordKill(player, "Goblin", 1, 304, 383));
        assertFalse(service.recordKill(player, "Brown Rat", 0, 304, 383));
        assertFalse(service.recordKill(player, "Brown Rat", 1, 425, 383));
        assertEquals(1, saves.get(), "rejected kills must not be persisted");

        assertTrue(service.recordKill(player, "Rat", 1, 304, 383),
                "the canonical Rat alias must count as Brown Rat");
        assertEquals(1, player.getQuestFlag(QuestService.killsFlag(QUEST)));
        assertEquals(2, saves.get());

        PlayerStateDto savedState = PlayerStateMapper.fromPlayer(player);
        Player restored = new Player();
        PlayerStateMapper.applyToPlayer(savedState, restored);
        assertEquals(QuestService.STATUS_ACTIVE,
                restored.getQuestFlag(QuestService.statusFlag(QUEST)));
        assertEquals(1, restored.getQuestFlag(QuestService.killsFlag(QUEST)));

        for (int i = 1; i < QUEST.getRequiredKills(); i++) {
            assertTrue(service.recordKill(restored, "Brown Rat", 1, 304, 383));
        }
        assertEquals(10, restored.getQuestFlag(QuestService.killsFlag(QUEST)));
        assertEquals(QuestService.STATUS_ACTIVE,
                restored.getQuestFlag(QuestService.statusFlag(QUEST)));
        assertEquals(0, restored.getGold(), "the tenth kill must not pay the reward");
        assertEquals(0, restored.getCurrentXp(), "the tenth kill must not pay XP");
        assertTrue(messages.get(messages.size() - 1).contains("Retournez voir"));

        assertTrue(service.giveOrReport(QUEST.getId(), QUEST.getGiverNpc(), restored)
                .contains("10/10"));
        assertEquals(QuestService.STATUS_ACTIVE,
                restored.getQuestFlag(QuestService.statusFlag(QUEST)));
        assertEquals(0, restored.getGold(), "GIVE_QUEST itself must not turn in a ready quest");
        assertNull(service.turnInReadyQuests("AnotherNpc", restored));

        restored.applyBuff("Double XP", "", "", null, true,
                List.of(new SpellData.SpellEffect("ATTRIBUTE", "exp", "100", "")));
        assertEquals(1f, restored.getBuffXpMultiplier());

        assertEquals("Réussite", service.turnInReadyQuests(QUEST.getGiverNpc(), restored));
        assertEquals(QuestService.STATUS_COMPLETED,
                restored.getQuestFlag(QuestService.statusFlag(QUEST)));
        assertEquals(10, restored.getQuestFlag(QuestService.killsFlag(QUEST)));
        assertEquals(500, restored.getGold());
        assertEquals(2, restored.getLevel());
        assertEquals(200, restored.getCurrentXp(),
                "300 exact XP at level 1 spends 100 on level-up and leaves 200");
        assertEquals(5, restored.getStatPoints());
        assertEquals(15, restored.getSkillPoints());
        assertEquals(12, saves.get(), "acceptance, ten kills and turn-in must each persist");

        assertNull(service.turnInReadyQuests(QUEST.getGiverNpc(), restored));
        assertEquals("Déjà terminée",
                service.giveOrReport(QUEST.getId(), QUEST.getGiverNpc(), restored));
        assertFalse(service.recordKill(restored, "Brown Rat", 1, 304, 383));
        assertEquals(500, restored.getGold());
        assertEquals(2, restored.getLevel());
        assertEquals(200, restored.getCurrentXp());
        assertEquals(12, saves.get(), "a completed one-time quest must never save or pay again");
    }
}
