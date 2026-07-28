package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.NpcDefBinaryIO;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.NpcDef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Adds Ortanalas' bridge-goblin quest without changing his existing topics. */
public final class OrtanalasGoblinQuestMigration {
    public static final String NPC_NAME = "Ortanalas";

    private OrtanalasGoblinQuestMigration() {
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
        System.out.println("Linked " + NPC_NAME + " to "
                + QuestCatalogueSeed.ORTANALAS_GOBLINS_QUEST_ID + " in " + target + ".");
    }

    static NpcDef update(NpcDef definition) {
        List<NpcDef.DialogTopic> topics = new ArrayList<>();
        for (NpcDef.DialogTopic topic : definition.getTopics()) {
            boolean oldQuestTopic = topic.getActions().stream()
                    .anyMatch(action -> action.getType() == ActionType.GIVE_QUEST
                            && action.getTargets().contains(
                            QuestCatalogueSeed.ORTANALAS_GOBLINS_QUEST_ID));
            if (!oldQuestTopic) {
                topics.add(topic);
            }
        }
        topics.add(new NpcDef.DialogTopic(
                List.of(
                        I18n.placeholder("npc.topic_keyword.ortanalas.goblins.0"),
                        I18n.placeholder("npc.topic_keyword.ortanalas.goblins.1"),
                        I18n.placeholder("npc.topic_keyword.ortanalas.goblins.2")
                ),
                null,
                List.of(new NpcDef.Action(ActionType.GIVE_QUEST,
                        List.of(QuestCatalogueSeed.ORTANALAS_GOBLINS_QUEST_ID)))
        ));
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
}
