package com.perso.T4C.quest;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import java.util.List;

/** The three camp assault described by Vincent Swiftblade. */
public final class MercenaryCampQuest {
  public static final int REQUIRED_SOLDIER_KILLS = 15;
  public static final String STAGE = "ADDON_CAMP_ASSAULT";
  public static final String LEADER_DEFEATED = "ADDON_MERCENARY_LEADER_DEFEAT";

  private MercenaryCampQuest() {}

  public static NpcScriptRuntime.Effects recordDeath(Player player, String monsterType) {
    if (player == null || monsterType == null) return NpcScriptRuntime.Effects.empty();
    int stage = player.getQuestFlag(STAGE);
    for (int camp = 0; camp < 3; camp++) {
      int activeStage = camp + 2;
      if (stage != activeStage) continue;
      String suffix = String.valueOf((char) ('A' + camp));
      String soldier = "MOBMERCENARY" + suffix;
      String lieutenant = "MOBMERCENARYLIEUTENANT" + suffix;
      String countFlag = "ADDON_MERCENARY_" + suffix + "_KILLS";
      String spawnFlag = "ADDON_MERCENARY_" + suffix + "_LIEUTENANT_SPAWNED";
      if (soldier.equals(monsterType)) {
        int count = Math.min(REQUIRED_SOLDIER_KILLS, player.getQuestFlag(countFlag) + 1);
        player.setQuestFlag(countFlag, count);
        if (count == REQUIRED_SOLDIER_KILLS
            && player.getQuestFlag(spawnFlag) == 0
            && summon(lieutenant, camp)) player.setQuestFlag(spawnFlag, 1);
      } else if (lieutenant.equals(monsterType) && player.getQuestFlag(spawnFlag) == 1) {
        player.setQuestFlag(STAGE, activeStage + 1);
        if (camp == 2) return spawnLeader(player);
      }
      return NpcScriptRuntime.Effects.empty();
    }
    if (stage == 5 && "MOBMERCENARYC".equals(monsterType)) return spawnLeader(player);
    if (stage == 5 && "MOBMERCENARYLEADER".equals(monsterType)) {
      player.setQuestFlag(LEADER_DEFEATED, 1);
      player.setQuestFlag(STAGE, 6);
    }
    return NpcScriptRuntime.Effects.empty();
  }

  private static NpcScriptRuntime.Effects spawnLeader(Player player) {
    if (player.getQuestFlag("ADDON_MERCENARY_LEADER_SPAWNED") != 0)
      return NpcScriptRuntime.Effects.empty();
    if (!NpcScriptRuntime.summon("MOBMERCENARYLEADER", 2730 * GRID_W, 900 * GRID_H, 0))
      return NpcScriptRuntime.Effects.empty();
    player.setQuestFlag("ADDON_MERCENARY_LEADER_SPAWNED", 1);
    return new NpcScriptRuntime.Effects(
        List.of(I18n.resolve("${npc.mercenary.camp.war_call}")), List.of(), List.of());
  }

  private static boolean summon(String monster, int camp) {
    int[] x = {2677, 2748, 2808};
    int[] y = {1072, 971, 970};
    return NpcScriptRuntime.summon(monster, x[camp] * GRID_W, y[camp] * GRID_H, 0);
  }

  public static void reset(Player player) {
    player.setQuestFlag(STAGE, 0);
    player.setQuestFlag(LEADER_DEFEATED, 0);
    player.setQuestFlag("ADDON_MERCENARY_LEADER_SPAWNED", 0);
    for (String suffix : List.of("A", "B", "C")) {
      player.setQuestFlag("ADDON_MERCENARY_" + suffix + "_KILLS", 0);
      player.setQuestFlag("ADDON_MERCENARY_" + suffix + "_LIEUTENANT_SPAWNED", 0);
    }
  }
}
