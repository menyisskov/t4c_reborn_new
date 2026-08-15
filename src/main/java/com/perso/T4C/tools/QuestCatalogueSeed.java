package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.QuestDefBinaryIO;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.quest.QuestDef;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Adds the built-in quests to the data-driven quest catalogue. */
public final class QuestCatalogueSeed {
    public static final String QUEST_ID = "lighthaven_samaritan_rats";
    public static final String ORTANALAS_GOBLINS_QUEST_ID = "ortanalas_bridge_goblins";

    private QuestCatalogueSeed() {
    }

    public static void main(String[] args) throws Exception {
        File target = new File(args.length > 0 ? args[0] : Paths.QUESTS_BIN);
        List<QuestDef> source = target.isFile() ? QuestDefBinaryIO.read(target) : List.of();
        List<QuestDef> definitions = upsert(source);
        QuestDefBinaryIO.write(target, definitions);
        System.out.println("Upserted built-in quests in " + target + ".");
    }

    static List<QuestDef> upsert(List<QuestDef> source) {
        List<QuestDef> definitions = new ArrayList<>(source == null ? List.of() : source);
        definitions.removeIf(definition -> definition != null
                && (QUEST_ID.equalsIgnoreCase(definition.getId())
                || ORTANALAS_GOBLINS_QUEST_ID.equalsIgnoreCase(definition.getId())));
        definitions.add(createDefinition());
        definitions.add(createOrtanalasGoblinDefinition());
        definitions.sort(Comparator.comparing(QuestDef::getId, String.CASE_INSENSITIVE_ORDER));
        return definitions;
    }

    static QuestDef createDefinition() {
        String keyPrefix = "quest." + QUEST_ID;
        return new QuestDef(
                QUEST_ID,
                I18n.placeholder(keyPrefix + ".title"),
                "LighthavenSamaritan",
                "Brown Rat",
                15,
                1,
                304,
                383,
                120,
                500,
                300,
                I18n.placeholder(keyPrefix + ".offer"),
                I18n.placeholder(keyPrefix + ".completion"),
                I18n.placeholder(keyPrefix + ".completed"),
                "__NEWBIE_QUEST"
        );
    }

    static QuestDef createOrtanalasGoblinDefinition() {
        String keyPrefix = "quest." + ORTANALAS_GOBLINS_QUEST_ID;
        return new QuestDef(
                ORTANALAS_GOBLINS_QUEST_ID,
                I18n.placeholder(keyPrefix + ".title"),
                OrtanalasGoblinQuestMigration.NPC_NAME,
                "Goblin",
                15,
                0,
                2760,
                1010,
                100,
                1_000,
                750,
                I18n.placeholder(keyPrefix + ".offer"),
                I18n.placeholder(keyPrefix + ".completion"),
                I18n.placeholder(keyPrefix + ".completed")
        );
    }
}
