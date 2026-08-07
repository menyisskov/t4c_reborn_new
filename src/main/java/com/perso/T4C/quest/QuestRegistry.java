package com.perso.T4C.quest;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.QuestDefBinaryIO;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Central access to the quest definitions stored in {@code quests.bin}. */
public final class QuestRegistry {
    private static List<QuestDef> cache;
    private static Map<String, QuestDef> byId;

    private QuestRegistry() {
    }

    public static synchronized List<QuestDef> load() {
        if (cache != null) {
            return cache;
        }
        File file = new File(Paths.QUESTS_BIN);
        List<QuestDef> definitions = List.of();
        if (file.exists()) {
            try {
                definitions = QuestDefBinaryIO.read(file);
            } catch (Exception ignored) {
                definitions = List.of();
            }
        }
        rebuild(definitions);
        return cache;
    }

    public static synchronized void save(List<QuestDef> definitions) throws IOException {
        QuestDefBinaryIO.write(new File(Paths.QUESTS_BIN), definitions);
        rebuild(definitions);
    }

    public static synchronized QuestDef findById(String id) {
        load();
        return id == null ? null : byId.get(normalize(id));
    }

    private static void rebuild(List<QuestDef> definitions) {
        cache = List.copyOf(definitions == null ? List.of() : definitions);
        Map<String, QuestDef> indexed = new LinkedHashMap<>();
        for (QuestDef definition : cache) {
            if (definition != null && definition.getId() != null) {
                indexed.put(normalize(definition.getId()), definition);
            }
        }
        byId = indexed;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
