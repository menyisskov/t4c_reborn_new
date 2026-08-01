package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.npc.NpcDef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Restores verified original appearances while preserving all NPC gameplay and dialogue data. */
public final class NpcOriginalAppearanceMigration {
    private NpcOriginalAppearanceMigration() {}

    public static void main(String[] args) throws Exception {
        File target = new File(args.length > 0 ? args[0] : Paths.NPCS_BIN);
        List<NpcDef> definitions = NpcDefBinaryIO.read(target);
        List<NpcDef> migrated = new ArrayList<>(definitions.size());
        int changed = 0;
        for (NpcDef definition : definitions) {
            try {
                NpcOriginalAppearance.Appearance appearance = NpcOriginalAppearance.forName(definition.getName());
                migrated.add(copyWithAppearance(definition, appearance));
                changed++;
            } catch (IllegalArgumentException ignored) {
                // Custom NPC: no original DLL/WDA entry, so its appearance must remain untouched.
                migrated.add(definition);
            }
        }
        NpcDefBinaryIO.write(target, migrated);
        System.out.println("Restored " + changed + " verified NPC appearances in " + target + ".");
    }

    static NpcDef copyWithAppearance(NpcDef definition, NpcOriginalAppearance.Appearance appearance) {
        return new NpcDef(definition.getName(), definition.getDisplayName(), appearance.parts(),
                appearance.spriteBase(), definition.getPatrolRadiusTiles(), definition.getFleeShouts(),
                definition.getWelcomeText(), definition.getTopics());
    }
}
