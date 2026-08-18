package com.perso.T4C.harvest.definition;

import com.perso.T4C.harvest.HerbDefinition;
import java.util.List;

public final class HerbDefinitions {
  private HerbDefinitions() {}

  public static List<HerbDefinition> all() {
    return List.of(
        Silverleaf.definition(),
        Emberbloom.definition(),
        Moonthistle.definition(),
        Goldroot.definition());
  }
}
