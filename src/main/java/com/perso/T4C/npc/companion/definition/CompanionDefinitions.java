package com.perso.T4C.npc.companion.definition;

import com.perso.T4C.npc.companion.CompanionDef;
import java.util.List;

public final class CompanionDefinitions {
  private CompanionDefinitions() {}

  public static List<CompanionDef> all() {
    return List.of(MageApprentice.definition(), WarriorSquire.definition());
  }
}
