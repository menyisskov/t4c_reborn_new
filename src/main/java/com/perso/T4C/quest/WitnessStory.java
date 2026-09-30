package com.perso.T4C.quest;

import com.perso.T4C.player.Player;

/** Optional mainland investigation; independent of classic quest, alignment and travel state. */
public final class WitnessStory {
  public static final String ARAKAS_RECORD = "witness_story.arakas_record";
  public static final String GROVE_TESTIMONY = "witness_story.grove_testimony";
  public static final String WAR_ACCOUNT = "witness_story.war_account";
  public static final String JUDGMENT_ACCOUNT = "witness_story.judgment_account";
  public static final String LIBRARY_COMPARISON = "witness_story.library_comparison";
  public static final String MANIFEST_COMPARED = "witness_story.manifest_compared";
  public static final String ORACLE_TESTIMONY = "witness_story.oracle_testimony";

  private WitnessStory() {}

  public static boolean has(Player player, String flag) {
    return player != null && player.getQuestFlag(flag) > 0;
  }

  /**
   * Records only known evidence, after its prerequisites, without consuming items or granting XP.
   */
  public static boolean record(Player player, String flag) {
    if (player == null || flag == null) return false;
    boolean available =
        switch (flag) {
          case ARAKAS_RECORD, ORACLE_TESTIMONY -> true;
          case GROVE_TESTIMONY -> has(player, ARAKAS_RECORD);
          case WAR_ACCOUNT, JUDGMENT_ACCOUNT -> has(player, GROVE_TESTIMONY);
          case LIBRARY_COMPARISON -> has(player, WAR_ACCOUNT) && has(player, JUDGMENT_ACCOUNT);
          case MANIFEST_COMPARED -> has(player, LIBRARY_COMPARISON);
          default -> false;
        };
    if (!available) return false;
    player.setQuestFlag(flag, 1);
    return true;
  }

  /**
   * Read-only recap points to the first missing piece, including both independent library voices.
   */
  public static String nextRouteKey(Player player) {
    if (!has(player, ARAKAS_RECORD)) return "npc.witness_story.route.arakas";
    if (!has(player, GROVE_TESTIMONY)) return "npc.witness_story.route.grove";
    if (!has(player, WAR_ACCOUNT)) return "npc.witness_story.route.war";
    if (!has(player, JUDGMENT_ACCOUNT)) return "npc.witness_story.route.judgment";
    if (!has(player, LIBRARY_COMPARISON)) return "npc.witness_story.route.compare";
    if (!has(player, MANIFEST_COMPARED)) return "npc.witness_story.route.manifest";
    return "npc.witness_story.route.isles";
  }

  /** Oracle's greeting consumes state 1/3 into 2, which is still a recorded victory. */
  public static boolean hasDefeatedMakrsh(Player player) {
    int state = player == null ? 0 : player.getQuestFlag("__FLAG_USER_HAS_KILLED_MAKRSH_PTANGH");
    return state >= 1 && state <= 3;
  }
}
