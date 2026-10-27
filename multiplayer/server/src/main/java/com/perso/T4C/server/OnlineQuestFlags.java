package com.perso.T4C.server;

import com.perso.T4C.quest.CrimsonscaleLetterRules;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestRules;
import java.util.HashMap;
import java.util.Map;

/**
 * Allowlist of quest flags the online server can run. Any flag outside it keeps melee refused
 * (unsupported_combat), so an unimplemented quest script can never be silently ignored.
 */
final class OnlineQuestFlags {
  private OnlineQuestFlags() {}

  static boolean supported(Map<String, Integer> flags, QuestCatalog catalog) {
    var statusFlags = new HashMap<String, Integer>();
    var killFlags = new HashMap<String, Integer>();
    for (QuestDef definition : catalog.definitions()) {
      statusFlags.put(QuestRules.statusFlag(definition), QuestRules.STATUS_COMPLETED);
      killFlags.merge(QuestRules.killsFlag(definition), definition.getRequiredKills(), Math::max);
    }
    for (var entry : flags.entrySet()) {
      String key = entry.getKey();
      Integer value = entry.getValue();
      if (key == null || value == null) return false;
      if (CrimsonscaleLetterRules.LETTER_FLAG.equals(key)) {
        if (value != 1) return false;
      } else if (statusFlags.containsKey(key)) {
        if (value < QuestRules.STATUS_NOT_STARTED || value > statusFlags.get(key)) return false;
      } else if (killFlags.containsKey(key)) {
        if (value < 0 || value > killFlags.get(key)) return false;
      } else if (key.startsWith(QuestRules.KILL_LOG_PREFIX)) {
        if (key.length() == QuestRules.KILL_LOG_PREFIX.length() || value < 0) return false;
      } else {
        return false;
      }
    }
    return true;
  }
}
