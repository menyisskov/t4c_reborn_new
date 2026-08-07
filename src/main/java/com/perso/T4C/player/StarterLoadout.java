package com.perso.T4C.player;

import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;

/** Development/player test loadout. Safe to call repeatedly (never duplicates unique entries). */
public final class StarterLoadout {
    public static final String LEVEL_UP_TEST_SPELL = "spell.level_up";

    private StarterLoadout() {}

    /** Keeps the temporary level-up test spell available without duplicating it. */
    public static void ensureLevelUpTestSpell(Player player) {
        SpellData levelUpSpell = SpellRegistry.findByName(LEVEL_UP_TEST_SPELL);
        if (player == null || levelUpSpell == null) return;

        if (player.getSpells() != null) {
            for (String known : player.getSpells()) {
                SpellData resolved = SpellRegistry.findByName(known);
                if (LEVEL_UP_TEST_SPELL.equalsIgnoreCase(known)
                        || (resolved != null && LEVEL_UP_TEST_SPELL.equals(resolved.getKey()))) return;
            }
        }

        var spells = player.getSpells() == null
                ? new java.util.ArrayList<String>()
                : new java.util.ArrayList<>(player.getSpells());
        spells.add(LEVEL_UP_TEST_SPELL);
        player.setSpells(spells);
    }

}
