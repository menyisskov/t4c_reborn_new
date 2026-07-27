package com.perso.T4C.tools;

import com.perso.T4C.helper.SpellBinaryIO;
import com.perso.T4C.spell.SpellData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * One-shot migration: adds (or fixes, if already present) a self-target buff
 * spell ("Fervor") that multiplies XP gain by 10 and grants unlimited HP/mana
 * for 1 hour, costs 0 mana, and uses the boulderFire- impact animation.
 * Idempotent. Run with --apply to write spells.bin.
 */
public final class NewXpBuffSpellMigration {

    private NewXpBuffSpellMigration() {
    }

    private static final String NAME = "Fervor";
    private static final String DESCRIPTION =
            "Fills the caster with zeal, multiplying experience gained by 10 and granting unlimited health and mana for a time.";
    /** One past the highest spellId observed in spells.bin at authoring time (10815). */
    private static final int SPELL_ID = 10900;
    private static final int DURATION_MILLIS = 3_600_000;
    private static final int XP_MULTIPLIER_PERCENT = 900; // +900% = x10 total

    public static void main(String[] args) throws Exception {
        boolean apply = args.length > 0 && "--apply".equals(args[0]);

        File file = new File("assets/spells/spells.bin");
        List<SpellData> current = SpellBinaryIO.read(file);
        SpellData fervor = new SpellData(
                NAME, DESCRIPTION, "0",
                0, 0, 0, 10,
                false, true,
                "64kSpellIconLightBoost", "64kSpellEnergyBallWhite-", "boulderFire-",
                0, 0,
                "Healing.wav", "Healing.wav",
                0, Integer.toString(DURATION_MILLIS), null, 0,
                null,
                SPELL_ID, 5, 3, 1,
                "100",
                "1000", "750", "750",
                30051, 0,
                false,
                List.of(
                        new SpellData.T4cEffect(2, List.of(
                                new SpellData.T4cEffect.EffectParam(1, null),
                                new SpellData.T4cEffect.EffectParam(2, "exp"),
                                new SpellData.T4cEffect.EffectParam(3, Integer.toString(XP_MULTIPLIER_PERCENT)))),
                        new SpellData.T4cEffect(2, List.of(
                                new SpellData.T4cEffect.EffectParam(1, null),
                                new SpellData.T4cEffect.EffectParam(2, "unlimited"),
                                new SpellData.T4cEffect.EffectParam(3, "1")))));

        List<SpellData> updated = new ArrayList<>(current);
        int existingIndex = -1;
        for (int i = 0; i < updated.size(); i++) {
            if (NAME.equals(updated.get(i).getName())) {
                existingIndex = i;
                break;
            }
        }
        if (existingIndex >= 0) {
            updated.set(existingIndex, fervor);
            System.out.println(NAME + ": existing entry replaced (id=" + SPELL_ID
                    + ", x10 XP, unlimited HP/mana, 0 mana cost, boulderFire- impact).");
        } else {
            updated.add(fervor);
            System.out.println(NAME + ": new spell added (id=" + SPELL_ID
                    + ", x10 XP, unlimited HP/mana, 0 mana cost, boulderFire- impact).");
        }

        if (apply) {
            SpellBinaryIO.write(file, updated);
            System.out.println("spells.bin updated.");
        } else {
            System.out.println("Dry run only — re-run with --apply to write spells.bin.");
        }
    }
}
