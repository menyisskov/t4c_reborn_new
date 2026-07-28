package com.perso.T4C.quest;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.QuestDefBinaryIO;
import com.perso.T4C.helper.SpawnBinaryIO;
import com.perso.T4C.i18n.I18n;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestRegistrySeedParityTest {
    @Test
    void lighthavenSamaritanRatQuestMatchesTheRequestedAsset() throws Exception {
        QuestDef quest = QuestDefBinaryIO.read(new File(Paths.QUESTS_BIN)).stream()
                .filter(definition -> "lighthaven_samaritan_rats".equals(definition.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("Les rats du sous-sol du temple", I18n.resolve(quest.getTitle()));
        assertEquals("LighthavenSamaritan", quest.getGiverNpc());
        assertEquals("Brown Rat", quest.getTargetMonster());
        assertEquals(10, quest.getRequiredKills());
        assertEquals(1, quest.getTargetWorldZ());
        assertEquals(304, quest.getAreaCenterX());
        assertEquals(383, quest.getAreaCenterY());
        assertEquals(120, quest.getAreaRadiusTiles());
        assertEquals(500, quest.getRewardGold());
        assertEquals(300, quest.getRewardXp());
        assertEquals("J'ai un travail pour vous. Allez dans le sous-sol du temple et tuez 10 rats, "
                        + "puis revenez me voir lorsque ce sera fait. Je vous donnerai une récompense.",
                I18n.resolve(quest.getOfferText()));
        assertNotNull(I18n.keyOf(quest.getTitle()));
        assertNotNull(I18n.keyOf(quest.getOfferText()));
        assertNotNull(I18n.keyOf(quest.getCompletionText()));
        assertNotNull(I18n.keyOf(quest.getCompletedText()));
        assertFalse(I18n.resolve(quest.getCompletionText()).contains("${"));
        assertFalse(I18n.resolve(quest.getCompletedText()).contains("${"));

        long matchingSpawns = SpawnBinaryIO.read(new File(Paths.MONSTER_SPAWNS_BIN)).stream()
                .filter(spawn -> spawn.z == quest.getTargetWorldZ())
                .filter(spawn -> "Rat".equals(spawn.type) || "Brown Rat".equals(spawn.type))
                .filter(spawn -> {
                    long dx = (long) spawn.x - quest.getAreaCenterX();
                    long dy = (long) spawn.y - quest.getAreaCenterY();
                    long radius = quest.getAreaRadiusTiles();
                    return dx * dx + dy * dy <= radius * radius;
                })
                .count();
        assertTrue(matchingSpawns >= quest.getRequiredKills(),
                "the configured objective area must contain enough Rat/Brown Rat spawns");
    }

    @Test
    void ortanalasGoblinQuestMatchesTheBridgeSpawns() throws Exception {
        QuestDef quest = QuestDefBinaryIO.read(new File(Paths.QUESTS_BIN)).stream()
                .filter(definition -> "ortanalas_bridge_goblins".equals(definition.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("Ortanalas", quest.getGiverNpc());
        assertEquals("Goblin", quest.getTargetMonster());
        assertEquals(15, quest.getRequiredKills());
        assertEquals(0, quest.getTargetWorldZ());
        assertEquals(2760, quest.getAreaCenterX());
        assertEquals(1010, quest.getAreaCenterY());
        assertEquals(100, quest.getAreaRadiusTiles());

        long matchingSpawns = SpawnBinaryIO.read(new File(Paths.MONSTER_SPAWNS_BIN)).stream()
                .filter(spawn -> spawn.z == quest.getTargetWorldZ())
                .filter(spawn -> quest.getTargetMonster().equals(spawn.type))
                .filter(spawn -> {
                    long dx = (long) spawn.x - quest.getAreaCenterX();
                    long dy = (long) spawn.y - quest.getAreaCenterY();
                    long radius = quest.getAreaRadiusTiles();
                    return dx * dx + dy * dy <= radius * radius;
                })
                .count();
        assertTrue(matchingSpawns >= quest.getRequiredKills(),
                "the bridge objective area must contain at least 15 Goblin spawns");
    }
}
