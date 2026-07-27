package com.perso.T4C.tools;

import com.perso.T4C.helper.SpellBinaryIO;
import com.perso.T4C.spell.SpellData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * One-shot migration: the Light spell is a free basic utility spell — it has no
 * price and no level/int/wis prerequisite. Clears those fields in spells.bin.
 * Run with --apply to write.
 */
public final class LightSpellRequirementMigration {

    private LightSpellRequirementMigration() {
    }

    public static void main(String[] args) throws Exception {
        boolean apply = args.length > 0 && "--apply".equals(args[0]);

        File file = new File("assets/spells/spells.bin");
        List<SpellData> current = SpellBinaryIO.read(file);
        List<SpellData> updated = new ArrayList<>(current.size());
        boolean changed = false;

        for (SpellData spell : current) {
            if (!"Light".equals(spell.getName())
                    || (spell.getPrice() == 0 && spell.getMinLevel() == 0
                        && spell.getMinInt() == 0 && spell.getMinWis() == 0)) {
                updated.add(spell);
                continue;
            }
            System.out.printf("Light: price %d->0, minLevel %d->0, minInt %d->0, minWis %d->0%n",
                    spell.getPrice(), spell.getMinLevel(), spell.getMinInt(), spell.getMinWis());
            updated.add(new SpellData(
                    spell.getName(), spell.getDescription(), spell.getManaCost(),
                    spell.getRadius(), 0, 0, 0,
                    spell.isAttack(), spell.isLineOfSight(),
                    spell.getIconId(), spell.getProjectileSpell(), spell.getImpactSpell(),
                    spell.getMinDamage(), spell.getMaxDamage(),
                    spell.getSound(), spell.getSoundImpact(),
                    spell.getCooldownSeconds(), spell.getDuration(), spell.getFrequency(), 0,
                    spell.getBuff(),
                    spell.getSpellId(), spell.getElement(), spell.getTargetType(), spell.getAttackType(),
                    spell.getSuccessRate(),
                    spell.getMentalExhaustion(), spell.getPhysicalExhaustion(), spell.getAttackExhaustion(),
                    spell.getVisualEffect(), spell.getVisualEffectTarget(),
                    spell.isPvp(), spell.getT4cEffects()));
            changed = true;
        }

        if (!changed) {
            System.out.println("Nothing to do: Light already has no price/requirements.");
            return;
        }
        if (apply) {
            SpellBinaryIO.write(file, updated);
            System.out.println("spells.bin updated.");
        } else {
            System.out.println("Dry run only — re-run with --apply to write spells.bin.");
        }
    }
}
