package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.npc.NpcDef;
import com.perso.T4C.player.BodyPart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies the exact puppet outfit declared for Samaritan in the original
 * Arakas {@code MonsterStatSetup.cpp}.
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
                null,
                definition.getPatrolRadiusTiles(),
                definition.getFleeShouts(),
                definition.getWelcomeText(),
                definition.getTopics()
        );
    }

    static List<NpcDef.Part> originalParts() {
        return List.of(
                new NpcDef.Part(BodyPart.BODY, "PupBodyClothSet1"),
                new NpcDef.Part(BodyPart.FEET, "PupBlackLeatherBoots"),
                new NpcDef.Part(BodyPart.HEAD, "PupElvenHat"),
                new NpcDef.Part(BodyPart.LEGS, "PupLeatherPants")
        );
    }
}
