package com.perso.T4C.gui.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuiDrawTest {
    @Test
    void percentageToAlphaClampsToValidRange() {
        assertEquals(0f, GuiDraw.percentageToAlpha(-1f), 0.001f);
        assertEquals(0.7f, GuiDraw.percentageToAlpha(70f), 0.001f);
        assertEquals(1f, GuiDraw.percentageToAlpha(120f), 0.001f);
        assertEquals(0f, GuiDraw.percentageToAlpha(Float.NaN), 0.001f);
    }
}
