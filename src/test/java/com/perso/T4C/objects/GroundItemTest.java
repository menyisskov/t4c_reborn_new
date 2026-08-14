package com.perso.T4C.objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroundItemTest {
    @Test
    void mapsOriginalInventoryQuiverToOriginalGroundQuiver() {
        assertEquals("64kItemGrQuiver", GroundItem.groundSpriteName("64kInvQuiver"));
    }

    @Test
    void keepsLegacyInventoryPrefixMapping() {
        assertEquals("Ground_Potion", GroundItem.groundSpriteName("Inv_Potion"));
    }
}
