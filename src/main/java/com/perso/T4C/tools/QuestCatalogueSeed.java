package com.perso.T4C.tools;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.QuestDefBinaryIO;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.quest.QuestDef;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Adds the Lighthaven Samaritan rat quest to the data-driven quest catalogue. */
public final class QuestCatalogueSeed {
    public static final String QUEST_ID = "lighthaven_samaritan_rats";

    private QuestCatalogueSeed() {
    }

    public static void main(String[] args) throws Exception {
        File target = new File(args.length > 0 ? args[0] : Paths.QUESTS_BIN);
        List<QuestDef> source = target.isFile() ? QuestDefBinaryIO.read(target) : List.of();
        List<QuestDef> definitions = upsert(source);
        QuestDefBinaryIO.write(target, definitions);
        System.out.println("Upserted " + QUEST_ID + " in " + target + ".");
    }

    static List<QuestDef> upsert(List<QuestDef> source) {
        List<QuestDef> definitions = new ArrayList<>(source == null ? List.of() : source);
        definitions.removeIf(definition -> definition != null
                && QUEST_ID.equalsIgnoreCase(definition.getId()));
        definitions.add(createDefinition());
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
                10,
                1,
                304,
                383,
                120,
                500,
                300,
                I18n.placeholder(keyPrefix + ".offer"),
                I18n.placeholder(keyPrefix + ".completion"),
                I18n.placeholder(keyPrefix + ".completed")
        );
    }
}
