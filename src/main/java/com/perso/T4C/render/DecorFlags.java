package com.perso.T4C.render;

import com.perso.T4C.mapping.definition.DecorLayerRuleDefinitions;
import java.util.HashSet;
import java.util.Set;

public final class DecorFlags {

  private DecorFlags() {}

  public static Set<String> loadPlayerAlwaysAboveRules() {
    return new HashSet<>(DecorLayerRuleDefinitions.all());
  }

  public static boolean isWalkableBridge(String name) {
    if (name == null || name.isBlank()) {
      return false;
    }
    int start = 0;
    while (start < name.length() && name.charAt(start) <= ' ') {
      start++;
    }
    return name.regionMatches(true, start, "bridge", 0, 6);
  }
}
