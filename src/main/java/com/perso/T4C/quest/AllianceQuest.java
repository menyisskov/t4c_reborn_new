package com.perso.T4C.quest;

import com.perso.T4C.player.Player;

/** Progress through Baldric, Doremas, and Grimish's Raven's Dust alliance errand. */
public final class AllianceQuest {
  public static final int REQUIRED_PIG_KILLS = 10;
  public static final int MINIMUM_LEVEL = 28;
  public static final int REWARD_XP = 70_000;

  public static final int NOT_STARTED = 0;
  public static final int KILL_PIGS = 1;
  public static final int REPORT_PIGS = 2;
  public static final int SEE_DOREMAS = 3;
  public static final int SEE_GRIMISH = 4;
  public static final int RETURN_TO_DOREMAS = 5;
  public static final int RETURN_TO_GRIMISH = 6;
  public static final int ANSWER_RIDDLE = 7;
  public static final int TELL_DOREMAS_CODE = 8;
  public static final int TELL_BALDRIC_CODE = 9;
  public static final int COMPLETE = 10;

  private static final String STAGE_FLAG = "__QUEST_ALLIANCE_STAGE";
  private static final String KILLS_FLAG = "__FLAG_COUNTER_PIG_KILLED";

  private AllianceQuest() {}

  public static int stage(Player player) {
    return player.getQuestFlag(STAGE_FLAG);
  }

  public static void stage(Player player, int stage) {
    player.setQuestFlag(STAGE_FLAG, stage);
  }

  public static int pigKills(Player player) {
    return player.getQuestFlag(KILLS_FLAG);
  }

  public static void recordPigKill(Player player) {
    if (player == null || stage(player) != KILL_PIGS) return;
    player.setQuestFlag(KILLS_FLAG, Math.min(REQUIRED_PIG_KILLS, pigKills(player) + 1));
  }

  public static void start(Player player) {
    if (stage(player) != NOT_STARTED) return;
    player.setQuestFlag(KILLS_FLAG, 0);
    stage(player, KILL_PIGS);
  }

  public static boolean reportPigs(Player player) {
    if (stage(player) != KILL_PIGS || pigKills(player) < REQUIRED_PIG_KILLS) return false;
    stage(player, REPORT_PIGS);
    player.setQuestFlag("__QUEST_KILL_THE_PIGS_COMPLETED", 1);
    return true;
  }
}
