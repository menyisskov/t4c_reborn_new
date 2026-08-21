// Generated from original T4C C++ NPC scripts. Do not edit by hand.
package com.perso.T4C.npc.script.original;

import com.perso.T4C.npc.core.NpcScripts;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OriginalNpcScriptSources {
  public static final int FORMAT_VERSION = 1;
  public static final String SOURCE_REPOSITORY = "https://github.com/elestranobaron/T4C-Serveur-Multiplateformes";
  public static final String SOURCE_COMMIT = "f4a5ed21db1cd21415825cc0e72ddb57beee053e";

  private OriginalNpcScriptSources() {}

  public static Map<String, Integer> macros() {
    return OriginalNpcScriptMacros.load();
  }

  public static Map<String, String> skillNames() {
    return OriginalNpcScriptSkills.load();
  }

  public static Map<String, NpcScripts.Entry> entries() {
    Map<String, NpcScripts.Entry> out = new LinkedHashMap<>();
    OriginalNpcScripts0.register(out);
    OriginalNpcScripts1.register(out);
    OriginalNpcScripts2.register(out);
    OriginalNpcScripts3.register(out);
    return Map.copyOf(out);
  }
}
