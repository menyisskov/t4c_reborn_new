package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.npc.NpcDef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * One-shot migration: moves Moonrock's panic lines out of the hardcoded
 * {@code NPCManager.FLEE_SHOUTS} constant into her definition in npcs.bin, so
 * the "flees when attacked" behaviour is data-driven and the text translatable
 * (keys {@code npc.flee_shout.moonrock.N}).
 * Idempotent. Run with --apply to write npcs.bin.
 */
public final class NpcFleeShoutsMigration {

    private NpcFleeShoutsMigration() {
    }

    private static final String NPC = "Moonrock";
    /** Verbatim from the constant this migration replaces; en.json/fr.json carry the translations. */
    private static final List<String> SHOUTS = List.of("AHHHHHHHHHHHHHH!", "HELP!");

    public static void main(String[] args) throws Exception {
        boolean apply = args.length > 0 && "--apply".equals(args[0]);

        File file = new File(Paths.NPCS_BIN);
        List<NpcDef> defs = new ArrayList<>(NpcDefBinaryIO.read(file));
        boolean changed = false;
        for (int i = 0; i < defs.size(); i++) {
            NpcDef d = defs.get(i);
            if (d == null || !NPC.equals(d.getName())) continue;
            if (SHOUTS.equals(d.getFleeShouts())) {
                System.out.println(NPC + ": already migrated, nothing to do.");
                return;
            }
            defs.set(i, new NpcDef(
                    d.getName(), d.getDisplayName(), d.getParts(), d.getSpriteBase(),
                    d.getDialogText(), d.getDialogKeyword(),
                    d.getAction(), d.getActionParam1(), d.getActionParam2(),
                    d.getPatrolRadiusTiles(), d.getTaughtSpells(),
                    d.getShopItems(), d.getTrainableStats(), SHOUTS));
            changed = true;
            System.out.println(NPC + ": flee shouts set -> " + SHOUTS);
        }
        if (!changed) {
            System.out.println(NPC + ": not found in npcs.bin, skipped.");
            return;
        }
        if (apply) {
            NpcDefBinaryIO.write(file, defs);
            System.out.println("npcs.bin updated.");
        } else {
            System.out.println("Dry run only — re-run with --apply to write npcs.bin.");
        }
    }
}
