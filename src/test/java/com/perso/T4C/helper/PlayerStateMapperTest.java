package com.perso.T4C.helper;

import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerStateMapperTest {

    @Test
    void preservesHalfTilePlayerCoordinates() throws Exception {
        Player player = new Player();
        player.setWorldPosition(3.5f * GRID_W, 4.5f * GRID_H, 0);

        PlayerStateDto state = PlayerStateMapper.fromPlayer(player);

        assertEquals(3.5f, state.x);
        assertEquals(4.5f, state.y);
    }
}
