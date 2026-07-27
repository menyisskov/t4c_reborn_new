package com.perso.T4C.spell;

import com.perso.T4C.helper.SpellBinaryIO;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellEffectManagerBuffTest {

    @Test
    void manaShieldCreatesVisibleBuffAndRaisesElementalResistances() throws Exception {
        SpellData manaShield = SpellBinaryIO.read(new File("assets/spells/spells.bin")).stream()
                .filter(spell -> "Mana shield".equals(spell.getName()))
                .findFirst()
                .orElseThrow();
        Player player = new Player();
        SpellEffectManager manager = new SpellEffectManager();

        List<SpellData.SpellEffect> effects = manager.resolvePlayerBuffEffects(manaShield, player);

        assertFalse(effects.isEmpty());
        assertEquals(5, effects.size());
        assertNotNull(manaShield.getIconId());
        player.applyBuff(manaShield.getName(), manaShield.getDescription(), manaShield.getIconId(),
                manager.resolveDurationSeconds(manaShield, player), false, effects);
        assertEquals(1, player.getActiveBuffs().size());
        assertEquals(133, player.getElementResistance("fire"));
        assertEquals(133, player.getElementResistance("dark"));
    }

    @Test
    void elementalPowerAndRadianceSpellsAlsoProduceVisibleBuffEffects() throws Exception {
        List<SpellData> spells = SpellBinaryIO.read(new File("assets/spells/spells.bin"));
        Player player = new Player();
        SpellEffectManager manager = new SpellEffectManager();

        for (String spellName : List.of("Mana surge", "Light")) {
            SpellData spell = spells.stream().filter(value -> spellName.equals(value.getName())).findFirst().orElseThrow();
            assertFalse(manager.resolvePlayerBuffEffects(spell, player).isEmpty(), spellName);
        }
    }

    @Test
    void everyKnownT4cAttributeBoostProducesAVisibleEffect() throws Exception {
        Player player = new Player();
        SpellEffectManager manager = new SpellEffectManager();
        for (SpellData spell : SpellBinaryIO.read(new File("assets/spells/spells.bin"))) {
            boolean containsAttributeBoost = spell.getT4cEffects().stream()
                    .anyMatch(effect -> effect != null && effect.getEffectType() == 2);
            // Instantaneous restores (mana potions) legitimately produce no buff; see
            // instantaneousManaRestoreProducesNoBuff.
            if (containsAttributeBoost && manager.resolveDurationSeconds(spell, player) > 0) {
                assertFalse(manager.resolvePlayerBuffEffects(spell, player).isEmpty(), spell.getName());
            }
        }
    }

    /**
     * A mana potion restores mana through resolvePlayerManaDelta only. Emitting a buff
     * effect for it too would both double-count the restore and, because its duration is
     * zero (which applyBuff reads as "unlimited"), leave a permanent icon on the buff bar.
     */
    @Test
    void instantaneousManaRestoreProducesNoBuff() throws Exception {
        SpellData potion = SpellBinaryIO.read(new File("assets/spells/spells.bin")).stream()
                .filter(spell -> "ITEM: Potion of mana".equals(spell.getName()))
                .findFirst()
                .orElseThrow();
        Player player = new Player();
        SpellEffectManager manager = new SpellEffectManager();

        assertEquals(0, manager.resolveDurationSeconds(potion, player));
        assertTrue(manager.resolvePlayerBuffEffects(potion, player).isEmpty());
        // The restore itself must still be resolved.
        assertEquals(25, manager.resolvePlayerManaDelta(potion, player));
    }

    /** A timed mana drain (manabane) stays a real, visible buff. */
    @Test
    void timedManaEffectRemainsAVisibleBuff() throws Exception {
        SpellData manabane = SpellBinaryIO.read(new File("assets/spells/spells.bin")).stream()
                .filter(spell -> "Mob manabane spell".equals(spell.getName()))
                .findFirst()
                .orElseThrow();
        Player player = new Player();
        SpellEffectManager manager = new SpellEffectManager();

        assertFalse(manager.resolvePlayerBuffEffects(manabane, player).isEmpty());
    }
}
