package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.npc.NpcDef;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies the effective Samaritan appearance declared by the original Arakas DLL.
 */
public final class LighthavenSamaritanAppearanceMigration {
    private LighthavenSamaritanAppearanceMigration() {
    }

    public static void main(String[] args) throws Exception {
        File target = new File(args.length > 0 ? args[0] : Paths.NPCS_BIN);
        List<NpcDef> definitions = NpcDefBinaryIO.read(target);
        List<NpcDef> migrated = new ArrayList<>(definitions.size());
        boolean found = false;
        for (NpcDef definition : definitions) {
            if (definition != null
                    && LighthavenSamaritanQuestMigration.NPC_NAME.equalsIgnoreCase(definition.getName())) {
                migrated.add(update(definition));
                found = true;
            } else {
                migrated.add(definition);
            }
        }
        if (!found) {
            throw new IllegalStateException("NPC not found in " + target + ": "
                    + LighthavenSamaritanQuestMigration.NPC_NAME);
        }
        NpcDefBinaryIO.write(target, migrated);
        System.out.println("Applied original Samaritan appearance in " + target + ".");
    }

    static NpcDef update(NpcDef definition) {
        return new NpcDef(
                definition.getName(),
                definition.getDisplayName(),
                originalParts(),
                NpcOriginalAppearance.forName(LighthavenSamaritanQuestMigration.NPC_NAME).spriteBase(),
                definition.getPatrolRadiusTiles(),
                definition.getFleeShouts(),
                definition.getWelcomeText(),
                definition.getTopics()
        );
    }

    static List<NpcDef.Part> originalParts() {
        return NpcOriginalAppearance.forName(LighthavenSamaritanQuestMigration.NPC_NAME).parts();
    }
}
