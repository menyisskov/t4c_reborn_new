package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.npc.NpcDef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Makes Vortimer send the player to the original GoN mad-house entrance destination. */
public final class WardenVortimerAsylumMigration {
    static final String NPC_NAME = "WardenVortimer";
    static final String TELEPORT = "TELEPORT(2704, 2226, 0)";
    static final String FRENCH_ENTER = "\"ENTRER\"";

    private WardenVortimerAsylumMigration() {
    }

    public static void main(String[] args) throws Exception {
        File target = new File(args.length > 0 ? args[0] : Paths.NPCS_BIN);
        List<NpcDef> migrated = new ArrayList<>();
        boolean found = false;
        for (NpcDef definition : NpcDefBinaryIO.read(target)) {
            if (definition != null && NPC_NAME.equalsIgnoreCase(definition.getName())) {
                migrated.add(update(definition));
                found = true;
            } else {
                migrated.add(definition);
            }
        }
        if (!found) throw new IllegalStateException("NPC not found in " + target + ": " + NPC_NAME);
        NpcDefBinaryIO.write(target, migrated);
        System.out.println("Vortimer now teleports to GoN's Madrigan asylum destination in " + target + ".");
    }

    static NpcDef update(NpcDef definition) {
        String script = definition.getSourceScript();
        if (script == null || script.isBlank()) {
            throw new IllegalStateException(NPC_NAME + " has no legacy dialogue script");
        }
        if (!script.contains(TELEPORT)) {
            Matcher entrance = Pattern.compile("(?m)^.*Command3\\(.*\"DOOR\".*\"KEY\".*\"ENTER\".*\\).*$")
                    .matcher(script);
            if (!entrance.find()) throw new IllegalStateException("Vortimer entrance command was not found");
            int bodyStart = entrance.end();
            script = script.substring(0, bodyStart) + "\n\t\t" + TELEPORT + script.substring(bodyStart);
        }
        Matcher entrance = Pattern.compile("Command3\\(([^\\r\\n]*\"DOOR\"[^\\r\\n]*\"KEY\"[^\\r\\n]*\"ENTER\"[^\\r\\n]*)\\)")
                .matcher(script);
        if (entrance.find()) {
            script = entrance.replaceFirst(Matcher.quoteReplacement(
                    "Command4(" + entrance.group(1) + ", " + FRENCH_ENTER + ")"));
        } else if (!script.contains(FRENCH_ENTER)) {
            throw new IllegalStateException("Vortimer's French entrance alias could not be added");
        }
        return new NpcDef(definition.getName(), definition.getDisplayName(), definition.getParts(),
                definition.getSpriteBase(), definition.getPatrolRadiusTiles(), definition.getFleeShouts(),
                definition.getWelcomeText(), definition.getTopics(), definition.getSourceTemplate(), script,
                definition.getSourceEvents());
    }
}
