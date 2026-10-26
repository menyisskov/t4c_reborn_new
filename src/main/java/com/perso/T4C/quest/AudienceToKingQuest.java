package com.perso.T4C.quest;

import com.perso.T4C.player.Player;

/** The timed Rogue Mage task that grants an audience with King Theodore. */
public final class AudienceToKingQuest {
  public static final int REQUIRED_KILLS = 10;
  public static final int TIME_LIMIT_SECONDS = 30 * 60;
  private static final String DEADLINE_FLAG = "__QUEST_KING_AUDIENCE_DEADLINE";
  private static final String KILLS_FLAG = "__QUEST_KING_AUDIENCE_KILLS";
  private static final String COMPLETE_FLAG = "__QUEST_KING_AUDIENCE_COMPLETE";

  private AudienceToKingQuest() {}

  public static int nowSeconds() {
    return (int) (System.currentTimeMillis() / 1000L);
  }

  public static boolean isComplete(Player player) {
    return player.getQuestFlag(COMPLETE_FLAG) != 0;
  }

  public static boolean isActive(Player player, int nowSeconds) {
    int deadline = player.getQuestFlag(DEADLINE_FLAG);
    return !isComplete(player) && deadline > 0 && nowSeconds <= deadline;
  }

  public static boolean isExpired(Player player, int nowSeconds) {
    int deadline = player.getQuestFlag(DEADLINE_FLAG);
    return !isComplete(player) && deadline > 0 && nowSeconds > deadline;
  }

  public static int kills(Player player) {
    return player.getQuestFlag(KILLS_FLAG);
  }

  public static void accept(Player player, int nowSeconds) {
    if (isComplete(player) || isActive(player, nowSeconds)) return;
    player.setQuestFlag(KILLS_FLAG, 0);
    player.setQuestFlag(DEADLINE_FLAG, nowSeconds + TIME_LIMIT_SECONDS);
  }

  public static void recordRogueMageKill(Player player, int nowSeconds) {
    if (player == null || !isActive(player, nowSeconds)) return;
    player.setQuestFlag(KILLS_FLAG, Math.min(REQUIRED_KILLS, kills(player) + 1));
  }

  public static boolean turnIn(Player player, int nowSeconds) {
    if (!isActive(player, nowSeconds) || kills(player) < REQUIRED_KILLS) return false;
    player.setQuestFlag(COMPLETE_FLAG, 1);
    player.setQuestFlag(DEADLINE_FLAG, 0);
    return true;
  }
}
