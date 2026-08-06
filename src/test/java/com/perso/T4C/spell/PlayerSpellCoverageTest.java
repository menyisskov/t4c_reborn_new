package com.perso.T4C.spell;

import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerSpellCoverageTest {
    private static final Set<Integer> SUPPORTED_EFFECT_TYPES = Set.of(
            1, 2, 3, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17);

    @Test
    void everyPlayerSpellUsesOnlyImplementedEffectFamilies() {
        Set<Integer> unsupported = new TreeSet<>();
        for (SpellData spell : SpellRegistry.playerCastableSpells()) {
            for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
                if (effect != null && !SUPPORTED_EFFECT_TYPES.contains(effect.getEffectType())) {
                    unsupported.add(effect.getEffectType());
                }
            }
        }
        assertTrue(unsupported.isEmpty(), "Unsupported player spell effect types: " + unsupported);
    }

    @Test
    void vaporizeIsRecognizedAsDestructive() {
        SpellData vaporize = SpellRegistry.findById(10210);
        assertTrue(new SpellEffectManager().hasVaporizeEffect(vaporize));
    }

    @Test
    void wordOfRecallUsesThePlayersRespawnLocation() {
        Player player = new Player();
        SpellEffectManager.PlayerUtility utility = new SpellEffectManager()
                .applyPlayerUtilityEffects(SpellRegistry.findById(10029), player);
        assertEquals(player.resolveRespawnWorldZ(), utility.teleportWorldZ());
    }

    @Test
    void detectionSpellsEnableTheirRuntimeFlags() {
        Player player = new Player();
        SpellEffectManager manager = new SpellEffectManager();
        manager.applyPlayerUtilityEffects(SpellRegistry.findById(10258), player);
        manager.applyPlayerUtilityEffects(SpellRegistry.findById(10259), player);
        assertTrue(player.canDetectInvisible());
        assertTrue(player.canDetectHidden());
    }
}
