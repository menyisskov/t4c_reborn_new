package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.npc.NpcDef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.perso.T4C.npc.ActionType;

/** Updates Sigfried's embedded legacy greeting so it advertises the repair dialogue keyword. */
public final class SigfriedRepairMigration {
    private static final String TARGET = "Sigfried";
    private static final String GREETING =
            "INTL(11780, \"Welcome to my \\\"shop\\\". I can sell you weapons or \\\"repair\\\" your damaged equipment.\")";
    private static final Pattern BEGIN_GREETING = Pattern.compile(
            "(?s)(\\bBegin\\s*)INTL\\(\\s*11780\\s*,.*?\\)\\s*(?=IF\\s*\\()",
            Pattern.CASE_INSENSITIVE);

    private SigfriedRepairMigration() {}

    public static void main(String[] args) throws Exception {
        File file = new File(Paths.NPCS_BIN);
        List<NpcDef> definitions = NpcDefBinaryIO.read(file);
        List<NpcDef> updated = new ArrayList<>(definitions.size());
        boolean found = false;
        for (NpcDef definition : definitions) {
            if (!TARGET.equalsIgnoreCase(definition.getName())) {
                updated.add(definition);
                continue;
            }
            found = true;
            String script = definition.getSourceScript();
            if (script == null) throw new IllegalStateException("Sigfried has no source script");
            Matcher matcher = BEGIN_GREETING.matcher(script);
            if (!matcher.find()) throw new IllegalStateException("Sigfried Begin greeting was not found");
            String migrated = matcher.replaceFirst(Matcher.quoteReplacement(matcher.group(1) + GREETING + "\n"));
            List<NpcDef.DialogTopic> topics = new ArrayList<>(definition.getTopics());
            boolean hasRepair = topics.stream().anyMatch(topic -> topic.getActions().stream()
                    .anyMatch(action -> action.getType() == ActionType.OPEN_REPAIR));
            if (!hasRepair) {
                topics.add(new NpcDef.DialogTopic(List.of("REPAIR"), null,
                        List.of(new NpcDef.Action(ActionType.OPEN_REPAIR))));
            }
            updated.add(new NpcDef(definition.getName(), definition.getDisplayName(), definition.getParts(),
                    definition.getSpriteBase(), definition.getPatrolRadiusTiles(), definition.getFleeShouts(),
                    definition.getWelcomeText(), topics, definition.getSourceTemplate(), migrated,
                    definition.getSourceEvents()));
        }
        if (!found) throw new IllegalStateException("Sigfried was not found in " + file);
        NpcDefBinaryIO.write(file, updated);
        System.out.println("Updated Sigfried's welcome and repair keyword in " + file);
    }
}
