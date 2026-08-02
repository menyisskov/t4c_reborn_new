package com.perso.T4C.helper;

import com.perso.T4C.player.Player;
import com.perso.T4C.combat.SeraphAuraService;
import org.junit.jupiter.api.Test;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void restoresCanonicalLightBuffWithItsRuntimeIdentityAndEffects() throws Exception {
        PlayerStateDto state = new PlayerStateDto();
        PlayerStateDto.ActiveBuffState light = new PlayerStateDto.ActiveBuffState();
        light.spellName = "spell.light";
        light.remainingSeconds = 299;
        light.totalDurationSeconds = 600;
        state.activeBuffs = java.util.List.of(light);

        Player restored = new Player();
        PlayerStateMapper.applyToPlayer(state, restored);

        assertEquals(1, restored.getActiveBuffs().size());
        assertEquals("${spell.light}", restored.getActiveBuffs().get(0).getSpellName());
        assertTrue(restored.hasRadianceBuff());
    }

    @Test
    void collapsesDuplicatePersistedSeraphAurasToTheCanonicalRuntimeBuff() throws Exception {
        PlayerStateDto state = new PlayerStateDto();
        state.rebirthCount = 1;
        PlayerStateDto.ActiveBuffState aura = new PlayerStateDto.ActiveBuffState();
        aura.spellName = SeraphAuraService.AURA_NAME;
        aura.remainingSeconds = Long.MAX_VALUE;
        aura.totalDurationSeconds = Long.MAX_VALUE;
        state.activeBuffs = java.util.List.of(aura, aura);

        Player restored = new Player();
        PlayerStateMapper.applyToPlayer(state, restored);
        SeraphAuraService.synchronize(restored);

        assertEquals(1, restored.getActiveBuffs().size());
        assertEquals(SeraphAuraService.AURA_NAME, restored.getActiveBuffs().get(0).getSpellName());
    }

}
