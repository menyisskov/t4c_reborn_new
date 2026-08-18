package com.perso.T4C.item;

import com.perso.T4C.content.ItemJavaExporter;
import com.perso.T4C.item.definition.ItemDefinitions;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ItemRegistry {
  private static List<ItemDefinition> cache;
  private static Map<String, ItemDefinition> byKey;
  private static Map<Integer, ItemDefinition> byNumId;

  private ItemRegistry() {}

  public static synchronized List<ItemDefinition> load() {
    if (cache != null) {
      return cache;
    }
    rebuild(ItemDefinitions.all());
    return cache;
  }

  public static synchronized void save(List<ItemDefinition> defs) throws IOException {
    ItemJavaExporter.export(defs);
    rebuild(defs);
  }

  public static synchronized ItemDefinition findByKey(String key) {
    load();
    return key == null ? null : byKey.get(ItemDefinition.normalizeKey(key));
  }

  public static synchronized ItemDefinition findByNumId(int numId) {
    load();
    return byNumId.get(numId);
  }

  public static synchronized Map<String, ItemDefinition> allByKey() {
    load();
    return byKey;
  }

  public static synchronized void invalidate() {
    cache = null;
    byKey = null;
    byNumId = null;
  }

  private static void rebuild(List<ItemDefinition> defs) {
    cache = List.copyOf(defs == null ? List.of() : defs);
    Map<String, ItemDefinition> map = new LinkedHashMap<>();
    Map<Integer, ItemDefinition> numeric = new LinkedHashMap<>();
    for (ItemDefinition def : cache) {
      if (def != null && def.getKey() != null) {
        map.put(def.getKey(), def);
        if (def.getNumId() > 0) numeric.putIfAbsent(def.getNumId(), def);
      }
    }
    byKey = Map.copyOf(map);
    byNumId = Map.copyOf(numeric);
  }
}
