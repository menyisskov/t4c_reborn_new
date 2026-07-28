package com.perso.T4C.screens;

import org.junit.jupiter.api.Test;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MainGameScreenDepthTest {

    @Test
    void playerDepthStartsOriginalOverlapTwoRowsAfterLogicalTile() {
        float worldY = 1060f * GRID_H + 7f;

        assertEquals(1062f, MainGameScreen.playerRenderDepth(worldY));
    }

    @Test
    void npcDepthUsesTheSameOverlapAnchorAsPlayer() {
        float npcWorldY = 1058f * GRID_H;

        assertEquals(1060f, MainGameScreen.playerRenderDepth((int) npcWorldY / GRID_H * GRID_H));
    }
}
