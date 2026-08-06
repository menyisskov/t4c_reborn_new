package com.perso.T4C.tools;

import com.perso.T4C.helper.SpellBinaryIO;
import com.perso.T4C.spell.SpellData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Restores GoN's iconless auxiliary movement-exhaust spell used by Entangle. */
public final class EntangleSpellMigration {
    private static final int ENTANGLE_MOVE_EXHAUST_ID = 10349;

    private EntangleSpellMigration() {
    }

    public static void main(String[] args) throws Exception {
        boolean apply = args.length > 0 && "--apply".equals(args[0]);
        File file = new File("assets/spells/spells.bin");
        List<SpellData> spells = new ArrayList<>(SpellBinaryIO.read(file));
        if (spells.stream().anyMatch(spell -> spell.getSpellId() == ENTANGLE_MOVE_EXHAUST_ID)) {
            System.out.println("Nothing to do: Entangle effect 2 is already present.");
            return;
        }

        spells.add(new SpellData(
                "${spell.entangle_effect_2}", "", "0",
                0, 0, 0, 0, false, false,
                "0", null, null, 0, 0, null, null,
                0, "0", "0", 0, null,
                ENTANGLE_MOVE_EXHAUST_ID, 2, 5, SpellData.ATTACK_PHYSICAL,
                "100", "0", "1750", "0", 0, 0, false, List.of()));

        if (apply) {
            SpellBinaryIO.write(file, spells);
            System.out.println("spells.bin updated with GoN spell 10349.");
        } else {
            System.out.println("Dry run only — re-run with --apply to write spells.bin.");
        }
    }
}
