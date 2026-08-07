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
 * Sets {@code @combat.passiveOnAttack} on the Dragon NPC (displayed in-game as "DarkFang"), the
 * one NPC that never fights back when attacked. Replaces the name check previously hardcoded in
 * {@code NPCManager}, which read this same fact from the source but stored it as a Java string
 * constant instead of NPC data.
 */
public final class PassiveOnAttackMigration {

    private static final String TARGET_NPC = "Dragon";

    private PassiveOnAttackMigration() {
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
            events.put("@combat.passiveOnAttack", "true");
            updated.add(new NpcDef(def.getName(), def.getDisplayName(), def.getParts(), def.getSpriteBase(),
                    def.getPatrolRadiusTiles(), def.getFleeShouts(), def.getWelcomeText(), def.getTopics(),
                    def.getSourceTemplate(), def.getSourceScript(), events));
        }

        if (!found) {
            System.out.println("NPC '" + TARGET_NPC + "' not found in " + file + "; nothing to do.");
            return;
        }
        if (dryRun) {
            System.out.println("(dry run) would set @combat.passiveOnAttack=true on " + TARGET_NPC);
            return;
        }
        NpcDefBinaryIO.write(file, updated);
        System.out.println("Wrote " + file + " with @combat.passiveOnAttack=true on " + TARGET_NPC);
    }
}
