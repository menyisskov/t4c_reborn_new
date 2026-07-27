package com.perso.T4C.helper;

import com.perso.T4C.config.Paths;
import com.perso.T4C.player.BodyPart;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pins the values that used to be hardcoded in {@code PlayerAppearanceDefaults},
 * {@code NPCAnimations}, {@code MainGameScreen} and the Content Studio front-end, so the
 * externalized asset cannot silently drift away from the original puppet.
 */
class AppearanceDefaultsCatalogTest {

    private static void assumeAssetPresent() {
        assumeTrue(new File(Paths.APPEARANCE_DEFAULTS_BIN).exists(), "appearance defaults asset missing");
    }

    @Test
    void maleNakedPartsMatchTheOriginalPuppet() {
        assumeAssetPresent();
        Map<BodyPart, String> expected = new LinkedHashMap<>();
        expected.put(BodyPart.BODY, "PupNakedBody");
        expected.put(BodyPart.HEAD, "PupNakedHead");
        expected.put(BodyPart.HAIR, "PupNormalHair");
        expected.put(BodyPart.LEGS, "PupNakedLegs");
        expected.put(BodyPart.FEET, "PupNakedFoot");
        expected.put(BodyPart.LEFT_ARM, "PupNakedArmL");
        expected.put(BodyPart.RIGHT_ARM, "PupNakedArmR");
        expected.put(BodyPart.LEFT_HAND, "PupNakedHandL");
        expected.put(BodyPart.RIGHT_HAND, "PupNakedHandR");

        assertEquals(expected, AppearanceDefaultsCatalog.nakedParts(AppearanceDefaultsCatalog.MALE));
    }

    @Test
    void femaleNakedPartsMatchTheOriginalPuppet() {
        assumeAssetPresent();
        Map<BodyPart, String> expected = new LinkedHashMap<>();
        expected.put(BodyPart.BODY, "WoNakedBody");
        expected.put(BodyPart.HEAD, "WoNakedHead");
        expected.put(BodyPart.HAIR, "WoHairPonyTail");
        expected.put(BodyPart.LEGS, "WoNakedLegs");
        expected.put(BodyPart.FEET, "WoNakedFeet");
        expected.put(BodyPart.LEFT_ARM, "WoNakedArmL");
        expected.put(BodyPart.RIGHT_ARM, "WoNakedArmR");
        expected.put(BodyPart.LEFT_HAND, "WoNakedHandL");
        expected.put(BodyPart.RIGHT_HAND, "WoNakedHandR");

        assertEquals(expected, AppearanceDefaultsCatalog.nakedParts(AppearanceDefaultsCatalog.FEMALE));
    }

    @Test
    void onlyPupHelmetsHideTheNakedHead() {
        assumeAssetPresent();
        for (String helmet : new String[] {
                "PupLeatherHelm", "PupPlateHelm", "PupChainMailCoif", "PupHornedHelmet" }) {
            assertTrue(AppearanceDefaultsCatalog.hidesHead(helmet), helmet);
        }
        for (String hat : new String[] { "V2_Haume01", "V2_Haume02", "V2_Haume04", "HalloweenHead" }) {
            assertFalse(AppearanceDefaultsCatalog.hidesHead(hat), hat);
        }
    }

    @Test
    void everyRegisteredHeadCoveringHidesTheHair() {
        assumeAssetPresent();
        for (String hat : new String[] {
                "PupLeatherHelm", "PupPlateHelm", "PupChainMailCoif", "PupHornedHelmet",
                "V2_Haume01", "V2_Haume02", "V2_Haume04", "HalloweenHead" }) {
            assertTrue(AppearanceDefaultsCatalog.hidesHair(hat), hat);
        }
    }

    @Test
    void unknownAppearancesAndGendersDegradeQuietly() {
        assumeAssetPresent();
        assertFalse(AppearanceDefaultsCatalog.hidesHead(null));
        assertFalse(AppearanceDefaultsCatalog.hidesHair("NotAHelmet"));
        assertEquals(Map.of(), AppearanceDefaultsCatalog.nakedParts("ORC"));
        assertEquals(Map.of(), AppearanceDefaultsCatalog.nakedParts(null));
    }
}
