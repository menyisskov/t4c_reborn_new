package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.NpcDef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Replaces only the Samaritan's work-topic response with the rat quest action.
 * Appearance, placement data and every other dialogue topic remain untouched.
 */
public final class LighthavenSamaritanQuestMigration {
    public static final String NPC_NAME = "LighthavenSamaritan";

    private LighthavenSamaritanQuestMigration() {
    }

    public static void main(String[] args) throws Exception {
        File target = new File(args.length > 0 ? args[0] : Paths.NPCS_BIN);
        List<NpcDef> definitions = NpcDefBinaryIO.read(target);
        List<NpcDef> migrated = new ArrayList<>(definitions.size());
        boolean found = false;
        for (NpcDef definition : definitions) {
            if (definition != null && NPC_NAME.equalsIgnoreCase(definition.getName())) {
                migrated.add(update(definition));
                found = true;
            } else {
                migrated.add(definition);
            }
        }
        if (!found) {
            throw new IllegalStateException("NPC not found in " + target + ": " + NPC_NAME);
        }
        NpcDefBinaryIO.write(target, migrated);
        System.out.println("Linked " + NPC_NAME + " to " + QuestCatalogueSeed.QUEST_ID + " in " + target + ".");
    }

    static NpcDef update(NpcDef definition) {
        List<NpcDef.DialogTopic> topics = new ArrayList<>(definition.getTopics().size());
        int workTopics = 0;
        for (NpcDef.DialogTopic topic : definition.getTopics()) {
            if (!isWorkTopic(topic)) {
                topics.add(topic);
                continue;
            }
            workTopics++;
            List<NpcDef.Action> actions = new ArrayList<>();
            for (NpcDef.Action action : topic.getActions()) {
                if (action.getType() != ActionType.GIVE_QUEST) {
                    actions.add(action);
                }
            }
            actions.add(new NpcDef.Action(ActionType.GIVE_QUEST,
                    List.of(QuestCatalogueSeed.QUEST_ID)));
            topics.add(new NpcDef.DialogTopic(topic.getKeywords(), null, actions));
        }
        if (workTopics != 1) {
            throw new IllegalStateException("Expected exactly one work topic for "
                    + NPC_NAME + ", found " + workTopics);
        }
        return new NpcDef(
                definition.getName(),
                definition.getDisplayName(),
                definition.getParts(),
                definition.getSpriteBase(),
                definition.getPatrolRadiusTiles(),
                definition.getFleeShouts(),
                definition.getWelcomeText(),
                topics
        );
    }

    private static boolean isWorkTopic(NpcDef.DialogTopic topic) {
        for (String keyword : topic.getKeywords()) {
            String resolved = I18n.resolve(keyword);
            if (resolved != null && "travail".equals(resolved.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
