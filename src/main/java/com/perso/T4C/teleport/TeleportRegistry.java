package com.perso.T4C.teleport;

import com.perso.T4C.teleport.definition.TeleportDefinitions;
import java.util.List;

public final class TeleportRegistry {
  private static List<TeleportDefinition> cache;

  private TeleportRegistry() {}

  public static synchronized List<TeleportDefinition> load() {
    return cache == null ? (cache = List.copyOf(TeleportDefinitions.all())) : cache;
  }

  public static synchronized void save(List<TeleportDefinition> definitions) {
    cache = List.copyOf(definitions == null ? List.of() : definitions);
  }
}
