package com.perso.T4C.quest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Pure quest progress rules shared by offline play and the authoritative server. Flags are read
 * through a caller-supplied accessor (a {@code Player} or a server draft), and every mutation is
 * returned as a map of flag writes for the caller to apply, so no caller state is touched here.
 */
public final class QuestRules {
  public static final int STATUS_NOT_STARTED = 0;
  public static final int STATUS_ACTIVE = 1;
  public static final int STATUS_COMPLETED = 2;
  public static final String KILL_LOG_PREFIX = "killlog.";

  private QuestRules() {}

  public static String statusFlag(QuestDef definition) {
    return "quest." + definition.getId() + ".status";
  }

  /** Quests sharing an activation flag share one kill counter, keyed by that flag. */
  public static String killsFlag(QuestDef definition) {
    String progressKey = definition.getActivationFlag();
    if (progressKey == null || progressKey.isBlank()) {
      progressKey = definition.getId();
    }
    return "quest." + progressKey + ".kills";
  }

  /** Kill-log flag for an already-canonical monster name. */
  public static String killLogFlag(String canonicalMonsterName) {
    return KILL_LOG_PREFIX + canonicalMonsterName.trim();
  }

  public static int status(ToIntFunction<String> flags, QuestDef definition) {
    int status = flags.applyAsInt(statusFlag(definition));
    if (status != STATUS_NOT_STARTED) return status;
    String activationFlag = definition.getActivationFlag();
    if (activationFlag != null
        && !activationFlag.isBlank()
        && flags.applyAsInt(activationFlag) != 0) {
      return STATUS_ACTIVE;
    }
    return STATUS_NOT_STARTED;
  }

  /** Kill count clamped to the quest's requirement. */
  public static int kills(ToIntFunction<String> flags, QuestDef definition) {
    return Math.max(
        0, Math.min(definition.getRequiredKills(), flags.applyAsInt(killsFlag(definition))));
  }

  /** Circle test on the monster's tile: a tile on the radius edge counts, one beyond does not. */
  public static boolean inObjectiveArea(QuestDef definition, int worldZ, int tileX, int tileY) {
    if (worldZ != definition.getTargetWorldZ()) {
      return false;
    }
    long dx = (long) tileX - definition.getAreaCenterX();
    long dy = (long) tileY - definition.getAreaCenterY();
    long radius = Math.max(0, definition.getAreaRadiusTiles());
    return dx * dx + dy * dy <= radius * radius;
  }

  /** Accepting a not-started quest starts it at zero kills; any other status writes nothing. */
  public static Map<String, Integer> accept(ToIntFunction<String> flags, QuestDef definition) {
    Map<String, Integer> writes = new LinkedHashMap<>();
    if (status(flags, definition) == STATUS_NOT_STARTED) {
      writes.put(statusFlag(definition), STATUS_ACTIVE);
      writes.put(killsFlag(definition), 0);
    }
    return writes;
  }

  /**
   * Counts one kill toward an active quest whose target matched and whose circle holds the kill.
   * Writes nothing once the requirement is met, so kills never exceed the requirement.
   */
  public static Map<String, Integer> creditObjective(
      ToIntFunction<String> flags,
      QuestDef definition,
      boolean targetMatches,
      int worldZ,
      int tileX,
      int tileY) {
    Map<String, Integer> writes = new LinkedHashMap<>();
    if (status(flags, definition) != STATUS_ACTIVE
        || !targetMatches
        || !inObjectiveArea(definition, worldZ, tileX, tileY)) {
      return writes;
    }
    int current = kills(flags, definition);
    if (current >= definition.getRequiredKills()) {
      return writes;
    }
    writes.put(killsFlag(definition), Math.min(definition.getRequiredKills(), current + 1));
    return writes;
  }

  /** The kill log counts every kill, whether or not any quest matches it. */
  public static Map<String, Integer> recordKillLog(ToIntFunction<String> flags, String logFlag) {
    Map<String, Integer> writes = new LinkedHashMap<>();
    writes.put(logFlag, flags.applyAsInt(logFlag) + 1);
    return writes;
  }

  /** Flags a completed quest holds: completed status and a full kill count. */
  public static Map<String, Integer> completionFlags(QuestDef definition) {
    Map<String, Integer> writes = new LinkedHashMap<>();
    writes.put(statusFlag(definition), STATUS_COMPLETED);
    writes.put(killsFlag(definition), definition.getRequiredKills());
    return writes;
  }
}
