package com.perso.T4C.harvest;

import com.perso.T4C.harvest.definition.HerbDefinitions;
import java.util.List;

public final class HerbRegistry {
  private static List<HerbDefinition> cache;

  private HerbRegistry() {}

  public static synchronized List<HerbDefinition> load() {
    if (cache != null) return cache;
    return cache = List.copyOf(HerbDefinitions.all());
  }

  public static synchronized void save(List<HerbDefinition> definitions) {
    cache = List.copyOf(definitions == null ? List.of() : definitions);
  }

  public static synchronized void invalidate() {
    cache = null;
  }
}
