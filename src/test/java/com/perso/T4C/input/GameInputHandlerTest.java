package com.perso.T4C.input;

import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GameInputHandlerTest {
    @Test
    void diagonalAdvancesEquallyOnBothGridAxes() {
        Vector2 direction = GameInputHandler.projectGridDirection(1, 1, new Vector2());

        assertEquals(direction.x / GRID_W, direction.y / GRID_H, 0.00001f);
        assertEquals(2f, direction.x / direction.y, 0.00001f);
        assertEquals(1f, direction.x, 0.00001f);
        assertEquals(0.5f, direction.y, 0.00001f);
    }

    @Test
    void oppositeInputsCancelAndCardinalDirectionsUseTileAspect() {
        Vector2 stopped = GameInputHandler.projectGridDirection(0, 0, new Vector2(3f, 4f));
        assertEquals(0f, stopped.len2());

        Vector2 horizontal = GameInputHandler.projectGridDirection(1, 0, new Vector2());
        Vector2 vertical = GameInputHandler.projectGridDirection(0, 1, new Vector2());
        assertEquals(horizontal.x / GRID_W, vertical.y / GRID_H, 0.00001f);
    }
}
