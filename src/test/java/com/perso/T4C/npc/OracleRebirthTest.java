package com.perso.T4C.npc;

import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Oracle's rebirth guard reads {@code ACK_MAXREMORTS}. An unresolved identifier evaluates to 0,
 * which made {@code remorts >= ACK_MAXREMORTS} always true and blocked every rebirth.
 */
class OracleRebirthTest {

    private NpcDef oracle() throws Exception {
        return NpcDefBinaryIO.read(new File("assets/npcs/npcs.bin")).stream()
                .filter(def -> "Oracle".equalsIgnoreCase(def.getName()))
                .findFirst().orElseThrow();
    }

    private Player eligiblePlayer() {
        Player player = new Player();
        player.setLevel(100);
        player.setQuestFlag("__FLAG_USER_HAS_DEFEATED_ASSISTANT", 1);
        return player;
    }

    @Test
    void offersRebirthConfirmationToAnEligiblePlayer() throws Exception {
        LegacyNpcScriptEngine.Result result = LegacyNpcScriptEngine.respond(
                oracle().getSourceScript(), "Oracle", "ready to be reborn", eligiblePlayer());

        assertEquals("REBIRTH", result.pendingYesNo(),
                "the Oracle should ask for confirmation instead of refusing");
    }

    @Test
    void confirmingRebirthTeleportsAndIncrementsTheCounter() throws Exception {
        NpcDef oracle = oracle();
        Player player = eligiblePlayer();
        LegacyNpcScriptEngine.respond(oracle.getSourceScript(), "Oracle", "ready to be reborn", player);

        LegacyNpcScriptEngine.Result result = LegacyNpcScriptEngine.respondYesNo(
                oracle.getSourceScript(), "Oracle", "REBIRTH", true, player);

        assertTrue(result.handled());
        assertEquals(1, player.getRebirthCount());
        assertEquals(1315 * com.perso.T4C.config.GameConstants.GRID_W, player.getCoordinates().getX());
        assertEquals(920 * com.perso.T4C.config.GameConstants.GRID_H, player.getCoordinates().getY());
        assertEquals(1, player.getCoordinates().getZ());
    }

    @Test
    void highlightsCmdAndWordsIndividuallyButClicksSayTheWholeSentence() throws Exception {
        java.util.Map<String, String> keywords =
                LegacyNpcScriptEngine.keywords(oracle().getSourceScript());

        // The dialogue quotes "ready" ... "reborn" as separate words, so each must be highlightable.
        assertTrue(keywords.containsKey("READY"), "READY must be highlighted on its own");
        assertEquals("READY REBORN", keywords.get("READY"));
        // REBORN also starts a plain Command2 section, but the CmdAND sentence has to win.
        assertEquals("READY REBORN", keywords.get("REBORN"));
    }

    @Test
    void clickingACmdAndWordReachesTheRebirthOffer() throws Exception {
        LegacyNpcScriptEngine.Result result = LegacyNpcScriptEngine.respond(
                oracle().getSourceScript(),
                "Oracle",
                LegacyNpcScriptEngine.keywords(oracle().getSourceScript()).get("READY"),
                eligiblePlayer());

        assertEquals("REBIRTH", result.pendingYesNo());
    }

    @Test
    void rebirthOpensTheRitualSoAlphanCanGrantEnergy() throws Exception {
        NpcDef oracle = oracle();
        Player player = eligiblePlayer();
        LegacyNpcScriptEngine.respond(oracle.getSourceScript(), "Oracle", "ready to be reborn", player);
        LegacyNpcScriptEngine.respondYesNo(oracle.getSourceScript(), "Oracle", "REBIRTH", true, player);

        assertEquals(1, player.getQuestFlag("__FLAG_NUMBER_OF_REMORTS"),
                "the script-visible counter must follow the rebirth count");
        assertEquals(0, player.getQuestFlag("__FLAG_REMORT_PROCESS"));
        assertTrue(player.getQuestFlag("__FLAG_REMORT_POINTS") > 0,
                "Alphan only hands out energy that REMORT_TO credited");
    }

    @Test
    void alphanGrantsEnergyAfterARebirth() throws Exception {
        NpcDef alphan = NpcDefBinaryIO.read(new File("assets/npcs/npcs.bin")).stream()
                .filter(def -> "RemortNPC1".equals(def.getName())).findFirst().orElseThrow();
        Player player = eligiblePlayer();
        NpcDef oracle = oracle();
        LegacyNpcScriptEngine.respond(oracle.getSourceScript(), "Oracle", "ready to be reborn", player);
        LegacyNpcScriptEngine.respondYesNo(oracle.getSourceScript(), "Oracle", "REBIRTH", true, player);

        LegacyNpcScriptEngine.Result result = LegacyNpcScriptEngine.respond(
                alphan.getSourceScript(), "RemortNPC1", "assist", player);

        assertEquals(1, player.getQuestFlag("__FLAG_REMORT_PROCESS"),
                "asking Alphan for assistance starts the energy-spending stage");
        assertNotNull(result.text());
    }

    @Test
    void rebirthDemotesTheCharacterToAnInexperiencedAdventurer() throws Exception {
        NpcDef oracle = oracle();
        Player player = eligiblePlayer();
        player.setStrength(500);
        player.setWisdom(500);
        player.setCurrentXp(98791);
        player.setSkillLevel("attack", 40);
        player.setSpells(new java.util.ArrayList<>(java.util.List.of("spell.fireball")));
        player.setBaseElementResistance("fire", 180);
        player.setBaseElementPower("fire", 160);

        LegacyNpcScriptEngine.respond(oracle.getSourceScript(), "Oracle", "ready to be reborn", player);
        LegacyNpcScriptEngine.respondYesNo(oracle.getSourceScript(), "Oracle", "REBIRTH", true, player);

        assertEquals(1, player.getLevel(), "the Oracle demotes the player to level 1");
        assertEquals(0, player.getCurrentXp());
        assertTrue(player.getSpells().isEmpty(), "spells are lost with the former life");
        assertEquals(0, player.getSkillLevel("attack"));
        // First rebirth: attributes fall to the floor Betran prices against, 20 + remorts * 5.
        assertEquals(25, player.getStrength());
        assertEquals(25, player.getWisdom());
        // The stored deltas are wiped; what remains on top is the Seraph ring's own bonus, since
        // getElementResistance deliberately includes equipment.
        assertEquals(0, player.getQuestFlag("legacy:resist:fire"), "elemental deltas are cleared");
        assertEquals(0, player.getQuestFlag("legacy:power:fire"));
        assertEquals(100, player.getElementPower("fire"), "powers carry no equipment bonus here");
    }

    @Test
    void rebirthDropsWhatTheFormerLifeCarried() throws Exception {
        NpcDef oracle = oracle();
        Player player = eligiblePlayer();
        player.setHiddenFor(60_000L);
        player.setGold(1234);

        LegacyNpcScriptEngine.respond(oracle.getSourceScript(), "Oracle", "ready to be reborn", player);
        LegacyNpcScriptEngine.respondYesNo(oracle.getSourceScript(), "Oracle", "REBIRTH", true, player);

        assertTrue(player.getActiveBuffs().isEmpty(), "buffs do not survive the former life");
        assertEquals(0L, player.getHiddenRemainingMillis());
        // The legacy scripts never touch gold, so it carries over like the inventory.
        assertEquals(1234, player.getGold());
    }

    @Test
    void rebirthGrantsAndWearsTheMarksOfTheSeraph() throws Exception {
        NpcDef oracle = oracle();
        Player player = eligiblePlayer();

        LegacyNpcScriptEngine.respond(oracle.getSourceScript(), "Oracle", "ready to be reborn", player);
        LegacyNpcScriptEngine.respondYesNo(oracle.getSourceScript(), "Oracle", "REBIRTH", true, player);

        assertEquals("item.remort_white_wings",
                player.getEquippedItems().get(com.perso.T4C.player.BodyPart.BACK),
                "the Oracle promises wings as part of the Seraph heritage");
        assertEquals("item.ring_of_the_seraph",
                player.getEquippedItems().get(com.perso.T4C.player.BodyPart.RING1));
    }

    /**
     * A character demoted to level 1 cannot sustain the gear of its former life, so everything worn
     * goes back to the inventory. Only the marks of the Seraph stay on.
     */
    @Test
    void rebirthReturnsWornGearToTheInventoryButKeepsTheSeraphMarks() throws Exception {
        NpcDef oracle = oracle();
        Player player = eligiblePlayer();
        // Pick a helm the freshly-built player actually qualifies for: equip refuses gear whose
        // requirements are not met, which would make this test vacuous.
        String helm = com.perso.T4C.item.ItemRegistry.load().stream()
                .filter(def -> def.getBodyPart() == com.perso.T4C.player.BodyPart.HEAD)
                .map(com.perso.T4C.item.ItemDefinition::getKey)
                .filter(key -> {
                    com.perso.T4C.item.InventoryService.add(player, key);
                    boolean ok = com.perso.T4C.item.InventoryService
                            .validateEquip(player, com.perso.T4C.player.BodyPart.HEAD, key).success();
                    if (!ok) com.perso.T4C.item.InventoryService.destroyOne(player, key);
                    return ok;
                })
                .findFirst().orElseThrow();
        com.perso.T4C.item.InventoryService.equip(player, com.perso.T4C.player.BodyPart.HEAD, helm);
        assertEquals(helm, player.getEquippedItems().get(com.perso.T4C.player.BodyPart.HEAD),
                "precondition: the helm must be worn before the ritual");

        LegacyNpcScriptEngine.respond(oracle.getSourceScript(), "Oracle", "ready to be reborn", player);
        LegacyNpcScriptEngine.respondYesNo(oracle.getSourceScript(), "Oracle", "REBIRTH", true, player);

        assertEquals(null, player.getEquippedItems().get(com.perso.T4C.player.BodyPart.HEAD),
                "gear of the former life must come off");
        assertTrue(player.getInventory().contains(helm), "and must be returned, not destroyed");
        assertEquals("item.ring_of_the_seraph",
                player.getEquippedItems().get(com.perso.T4C.player.BodyPart.RING1),
                "the Seraph heritage survives the ritual");
        assertEquals("item.remort_white_wings",
                player.getEquippedItems().get(com.perso.T4C.player.BodyPart.BACK));
    }

    @Test
    void eachRebirthRaisesTheAttributeFloor() throws Exception {
        NpcDef oracle = oracle();
        Player player = eligiblePlayer();
        player.setQuestFlag("__FLAG_NUMBER_OF_REMORTS", 3);

        LegacyNpcScriptEngine.respond(oracle.getSourceScript(), "Oracle", "ready to be reborn", player);
        LegacyNpcScriptEngine.respondYesNo(oracle.getSourceScript(), "Oracle", "REBIRTH", true, player);

        assertEquals(4, player.getQuestFlag("__FLAG_NUMBER_OF_REMORTS"));
        assertEquals(40, player.getStrength(), "20 + 4 * 5 after the fourth rebirth");
    }

    @Test
    void alphanTeleportsOnceEveryPointIsSpent() throws Exception {
        NpcDef alphan = NpcDefBinaryIO.read(new File("assets/npcs/npcs.bin")).stream()
                .filter(def -> "RemortNPC1".equals(def.getName())).findFirst().orElseThrow();
        Player player = eligiblePlayer();
        player.setQuestFlag("__FLAG_REMORT_PROCESS", 1);
        player.setQuestFlag("__FLAG_REMORT_POINTS", 0);
        player.setWorldPosition(0, 0, 0);

        // Greeting Alphan with no energy left moves the ritual to its final stage.
        LegacyNpcScriptEngine.begin(alphan.getSourceScript(), "RemortNPC1", player);
        assertEquals(2, player.getQuestFlag("__FLAG_REMORT_PROCESS"));

        LegacyNpcScriptEngine.Result result = LegacyNpcScriptEngine.respond(
                alphan.getSourceScript(), "RemortNPC1", "begin", player);

        assertTrue(result.handled());
        // Leaving the ritual must reach the world, not the ritual room the Oracle sent us to.
        assertEquals(2939 * com.perso.T4C.config.GameConstants.GRID_W, player.getCoordinates().getX(),
                "the farewell speech must send the player back into the world");
        assertEquals(1066 * com.perso.T4C.config.GameConstants.GRID_H, player.getCoordinates().getY());
        assertEquals(0, player.getCoordinates().getZ());
        assertEquals(0, player.getQuestFlag("__FLAG_REMORT_PROCESS"),
                "the ritual must be closed so the next rebirth starts clean");
    }

    @Test
    void refusesRebirthBelowTheRequiredLevel() throws Exception {
        Player player = new Player();
        player.setLevel(74);
        player.setQuestFlag("__FLAG_USER_HAS_DEFEATED_ASSISTANT", 1);

        LegacyNpcScriptEngine.Result result = LegacyNpcScriptEngine.respond(
                oracle().getSourceScript(), "Oracle", "ready to be reborn", player);

        assertEquals(null, result.pendingYesNo());
        assertNotNull(result.text());
    }
}
