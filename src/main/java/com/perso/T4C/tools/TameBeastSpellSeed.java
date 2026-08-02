package com.perso.T4C.tools;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Adds the tame spell without replacing the rest of the spell catalogue. */
public final class TameBeastSpellSeed {
    private TameBeastSpellSeed() {}
    public static void main(String[] args) throws Exception {
        I18n.update(Map.of("spell.tame_beast", "Apprivoiser", "spell.tame_beast.desc", "Canalise pour apprivoiser une bête."));
        List<SpellData> spells = new ArrayList<>(SpellRegistry.load());
        spells.removeIf(s -> s != null && ("tame_beast".equals(s.getKey())
                || "spell.tame_beast".equals(s.getKey()) || s.getKey().endsWith(".tame_beast")));
        spells.add(new SpellData("spell.tame_beast", "${spell.tame_beast.desc}", "10", 0, 0, 0, 1,
                true, true, "64kSpellIconTameBeast", null, "HealingSpell-", 0, 0, null, null, 30, "5", null, 0, null,
                0, 0, 2, 0, "100", null, null, null, 0, 0, false, List.of()));
        SpellRegistry.save(spells);
        System.out.println("Seeded spell.tame_beast; catalogue size=" + spells.size());
    }
}
