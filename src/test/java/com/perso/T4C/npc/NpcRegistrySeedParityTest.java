package com.perso.T4C.npc;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks in the shop/train/action payloads that the removed {@code NpcSeed} used
 * to patch onto {@code npcs.bin} at load time. The data is now persisted in the
 * file itself, so these assertions guard against it being silently dropped by a
 * future re-import.
 *
 * <p>Reads the binary directly rather than through {@link NpcRegistry} so the
 * expectations describe the on-disk truth, not whatever the loader might layer
 * on top of it.
 */
class NpcRegistrySeedParityTest {

    private static Map<String, NpcDef> byName;

    @BeforeAll
    static void loadDefinitions() throws Exception {
        List<NpcDef> defs = NpcDefBinaryIO.read(new File(Paths.NPCS_BIN));
        Map<String, NpcDef> map = new LinkedHashMap<>();
        for (NpcDef def : defs) {
            if (def != null && def.getName() != null) {
                map.put(def.getName(), def);
            }
        }
        byName = map;
    }

    private static NpcDef require(String name) {
        NpcDef def = byName.get(name);
        assertNotNull(def, name + " is missing from " + Paths.NPCS_BIN);
        return def;
    }

    /** Formerly injected by {@code addMissingPlacedNpc} when absent from the file. */
    @Test
    void npcsOnceAddedAtLoadTimeArePersisted() {
        for (String name : List.of("Fali", "Kilhiam", "TwinNevanis", "TwinShovanis")) {
            NpcDef def = require(name);
            assertTrue(def.getParts() != null && !def.getParts().isEmpty()
                            || def.getSpriteBase() != null && !def.getSpriteBase().isBlank(),
                    name + " has no appearance (neither parts nor spriteBase)");
        }
    }

    @Test
    void faliSellsHerPotionStock() {
        NpcDef fali = require("Fali");
        assertEquals(KeywordActionType.SHOP, fali.getAction());
        assertShop(fali, Map.of(
                "Apple", 5L,
                "Light healing potion", 17L,
                "Healing potion", 42L,
                "Serious healing potion", 100L,
                "Potion of mana", 17L));
    }

    @Test
    void rolphSellsTheArmorCatalogue() {
        NpcDef rolph = require("Rolph");
        assertEquals(KeywordActionType.SHOP, rolph.getAction());
        assertEquals(16, rolph.getShopItems().size(), "Rolph shop size");
        assertShop(rolph, Map.of(
                "Cloth pants", 15L,
                "Leather armor", 607L,
                "Studded leather armor", 4538L,
                "Wooden shield", 3355L));
    }

    @Test
    void sigfriedSellsTheWeaponCatalogue() {
        NpcDef sigfried = require("Sigfried");
        assertEquals(KeywordActionType.SHOP, sigfried.getAction());
        assertEquals(13, sigfried.getShopItems().size(), "Sigfried shop size");
        assertShop(sigfried, Map.of(
                "Rusted short sword", 29L,
                "Rusted hand axe", 2361L,
                "Ashwood reflex bow", 2361L,
                "Wooden arrow", 100L));
    }

    @Test
    void trainersKeepTheirTrainableStats() {
        assertTrain(require("Murmuntag"), Map.of("attack", 10));
        assertTrain(require("Ortanalas"), Map.of(
                "attack", 10, "archery", 15, "stun_blow", 20, "powerful_blow", 50));
        assertTrain(require("Kalastor"), Map.of("peek", 25, "dodge", 10, "archery", 15));
        assertTrain(require("Mhorgwloth"), Map.of("rapid_healing", 200));
    }

    /**
     * Moonrock heals via CAST (see {@code NpcMoonrockHealMigration}), which
     * superseded the plain HEAL action the old seed still declared.
     */
    @Test
    void moonrockHealsByCastingHealCritical() {
        NpcDef moonrock = require("Moonrock");
        assertEquals(KeywordActionType.CAST, moonrock.getAction());
        assertEquals(1, moonrock.getTaughtSpells().size());
        assertEquals("Heal critical", moonrock.getTaughtSpells().get(0).getSpellName());
    }

    private static void assertShop(NpcDef def, Map<String, Long> expected) {
        Map<String, Long> actual = new LinkedHashMap<>();
        for (NpcDef.ShopItem item : def.getShopItems()) {
            actual.put(item.getItemKey(), item.getPrice());
        }
        expected.forEach((key, price) ->
                assertEquals(price, actual.get(key), def.getName() + " price for " + key));
    }

    private static void assertTrain(NpcDef def, Map<String, Integer> expected) {
        assertEquals(KeywordActionType.TRAIN, def.getAction(), def.getName() + " action");
        Map<String, Integer> actual = new LinkedHashMap<>();
        for (NpcDef.TrainableStat stat : def.getTrainableStats()) {
            actual.put(stat.getStatId(), stat.getCostPerPoint());
        }
        assertEquals(expected.size(), actual.size(), def.getName() + " trainable stat count");
        expected.forEach((id, cost) ->
                assertEquals(cost, actual.get(id), def.getName() + " cost for " + id));
    }
}
