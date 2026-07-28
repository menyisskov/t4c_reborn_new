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

    @Test
    void preservesQuestFlags() throws Exception {
        Player source = new Player();
        source.setQuestFlag("quest.lighthaven_samaritan_rats.status", 1);
        source.setQuestFlag("quest.lighthaven_samaritan_rats.kills", 7);

        PlayerStateDto state = PlayerStateMapper.fromPlayer(source);
        Player restored = new Player();
        PlayerStateMapper.applyToPlayer(state, restored);

        assertEquals(1, restored.getQuestFlag("quest.lighthaven_samaritan_rats.status"));
        assertEquals(7, restored.getQuestFlag("quest.lighthaven_samaritan_rats.kills"));
    }

}
