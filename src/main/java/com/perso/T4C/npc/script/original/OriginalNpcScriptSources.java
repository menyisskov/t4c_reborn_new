package com.perso.T4C.npc.script.original;

import java.util.Map;

/** Spell/skill macro tables used by Java NPC behaviors. Conversation lives in NPC classes. */
public final class OriginalNpcScriptSources {

  private OriginalNpcScriptSources() {}

  public static Map<String, Integer> macros() {
    return OriginalNpcScriptMacros.load();
  }

  public static Map<String, String> skillNames() {
    return OriginalNpcScriptSkills.load();
  }
}
