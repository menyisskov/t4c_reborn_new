package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.npc.NpcDef;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sets {@code @rebirth.regalia} on the Oracle NPC, the sole caster of the {@code REMORT_TO}
 * rebirth ritual. Replaces the item list previously hardcoded as {@code SERAPH_REGALIA} in
 * {@code NpcScriptEngine}.
 */
public final class RebirthRegaliaMigration {

    private static final String TARGET_NPC = "Oracle";
    private static final String REGALIA = "item.remort_white_wings,item.ring_of_the_seraph";

    private RebirthRegaliaMigration() {
    }

    public static void main(String[] args) throws Exception {
        boolean dryRun = args.length > 0 && "--dry-run".equals(args[args.length - 1]);
        File file = new File(Paths.NPCS_BIN);
        List<NpcDef> defs = NpcDefBinaryIO.read(file);

        List<NpcDef> updated = new ArrayList<>(defs.size());
        boolean found = false;
        for (NpcDef def : defs) {
            if (!def.getName().equalsIgnoreCase(TARGET_NPC)) {
                updated.add(def);
                continue;
            }
            found = true;
            Map<String, String> events = new HashMap<>(def.getSourceEvents());
            events.put("@rebirth.regalia", REGALIA);
            updated.add(new NpcDef(def.getName(), def.getDisplayName(), def.getParts(), def.getSpriteBase(),
                    def.getPatrolRadiusTiles(), def.getFleeShouts(), def.getWelcomeText(), def.getTopics(),
                    def.getSourceTemplate(), def.getSourceScript(), events));
        }

        if (!found) {
            System.out.println("NPC '" + TARGET_NPC + "' not found in " + file + "; nothing to do.");
            return;
        }
        if (dryRun) {
            System.out.println("(dry run) would set @rebirth.regalia=" + REGALIA + " on " + TARGET_NPC);
            return;
        }
        NpcDefBinaryIO.write(file, updated);
        System.out.println("Wrote " + file + " with @rebirth.regalia=" + REGALIA + " on " + TARGET_NPC);
    }
}
