package com.perso.T4C.quest;

import com.perso.T4C.quest.definition.QuestDefinitions;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class QuestRegistry {
  private static List<QuestDef> cache;
  private static Map<String, QuestDef> byId;

  private QuestRegistry() {}

  public static synchronized List<QuestDef> load() {
    if (cache != null) {
      return cache;
    }
    rebuild(QuestDefinitions.all());
    return cache;
  }

  public static synchronized void save(List<QuestDef> definitions) {
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
