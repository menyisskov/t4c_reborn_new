package com.perso.T4C.tools;

import com.perso.T4C.helper.SpellBinaryIO;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellProjectilePalette;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Restores energy-ball palette arguments that were lost by the sprite importer. */
public final class SpellProjectilePaletteMigration {
    private SpellProjectilePaletteMigration() {
    }

    public static void main(String[] args) throws Exception {
        boolean apply = args.length > 0 && "--apply".equals(args[0]);
        File file = new File("assets/spells/spells.bin");
        List<SpellData> updated = new ArrayList<>();
        int changed = 0;
        for (SpellData spell : SpellBinaryIO.read(file)) {
            String projectile = SpellProjectilePalette.projectileFor(
                    spell.getVisualEffect(), spell.getProjectileSpell());
            if (!Objects.equals(projectile, spell.getProjectileSpell())) changed++;
            updated.add(copyWithProjectile(spell, projectile));
        }
        if (apply) {
            SpellBinaryIO.write(file, updated);
            System.out.println("Updated " + changed + " spell projectile palettes.");
        } else {
            System.out.println("Dry run: " + changed + " spell projectile palettes to update.");
        }
    }

    private static SpellData copyWithProjectile(SpellData spell, String projectile) {
        return new SpellData(spell.getName(), spell.getDescription(), spell.getManaCost(),
                spell.getRadius(), spell.getMinInt(), spell.getMinWis(), spell.getMinLevel(),
                spell.isAttack(), spell.isLineOfSight(), spell.getIconId(), projectile,
                spell.getImpactSpell(), spell.getMinDamage(), spell.getMaxDamage(), spell.getSound(),
                spell.getSoundImpact(), spell.getCooldownSeconds(), spell.getDuration(),
                spell.getFrequency(), spell.getPrice(), spell.getBuff(), spell.getSpellId(),
                spell.getElement(), spell.getTargetType(), spell.getAttackType(), spell.getSuccessRate(),
                spell.getMentalExhaustion(), spell.getPhysicalExhaustion(), spell.getAttackExhaustion(),
                spell.getVisualEffect(), spell.getVisualEffectTarget(), spell.isPvp(), spell.getT4cEffects());
    }
}
