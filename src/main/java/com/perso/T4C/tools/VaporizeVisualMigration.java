package com.perso.T4C.tools;

import com.perso.T4C.helper.SpellBinaryIO;
import com.perso.T4C.spell.SpellData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Applies GoN visual 30081: yellow energy ball followed by Freeze. */
public final class VaporizeVisualMigration {
    private VaporizeVisualMigration() {
    }

    public static void main(String[] args) throws Exception {
        boolean apply = args.length > 0 && "--apply".equals(args[0]);
        File file = new File("assets/spells/spells.bin");
        List<SpellData> updated = new ArrayList<>();
        boolean changed = false;
        for (SpellData spell : SpellBinaryIO.read(file)) {
            if (spell.getSpellId() != 10210) {
                updated.add(spell);
                continue;
            }
            updated.add(new SpellData(spell.getName(), spell.getDescription(), spell.getManaCost(),
                    spell.getRadius(), spell.getMinInt(), spell.getMinWis(), spell.getMinLevel(),
                    spell.isAttack(), spell.isLineOfSight(), spell.getIconId(),
                    "64kSpellEnergyBallYellow-", "Freeze-", spell.getMinDamage(), spell.getMaxDamage(),
                    "Healing.wav", "Freeze.wav", spell.getCooldownSeconds(), spell.getDuration(),
                    spell.getFrequency(), spell.getPrice(), spell.getBuff(), spell.getSpellId(),
                    spell.getElement(), spell.getTargetType(), spell.getAttackType(), spell.getSuccessRate(),
                    spell.getMentalExhaustion(), spell.getPhysicalExhaustion(), spell.getAttackExhaustion(),
                    spell.getVisualEffect(), spell.getVisualEffectTarget(), spell.isPvp(), spell.getT4cEffects()));
            changed = !"64kSpellEnergyBallYellow-".equals(spell.getProjectileSpell())
                    || !"Freeze-".equals(spell.getImpactSpell());
        }
        if (!changed) {
            System.out.println("Nothing to do: Vaporize visuals are already aligned with GoN.");
        } else if (apply) {
            SpellBinaryIO.write(file, updated);
            System.out.println("spells.bin updated with GoN Vaporize visuals.");
        } else {
            System.out.println("Dry run only — re-run with --apply to write spells.bin.");
        }
    }
}
