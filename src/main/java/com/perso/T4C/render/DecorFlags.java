package com.perso.T4C.render;

import com.perso.T4C.mapping.definition.DecorLayerRuleDefinitions;
import java.util.HashSet;
import java.util.Set;

public final class DecorFlags {

  private DecorFlags() {}

  public static Set<String> loadPlayerAlwaysAboveRules() {
    return new HashSet<>(DecorLayerRuleDefinitions.all());
  }
}
