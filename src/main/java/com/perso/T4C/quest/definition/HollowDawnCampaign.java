package com.perso.T4C.quest.definition;

import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestService;
import java.util.List;

/** Two witness accounts converge on the final acts at Threnody Reach. */
public final class HollowDawnCampaign {
  private HollowDawnCampaign() {}

  public static final String CHRONICLER = "ChroniclerMaelin";
  public static final String KEEPER = "KeeperVael";

  public static List<QuestDef> avalon() {
    return List.of(
        stage(
            "moonwake_missing",
            CHRONICLER,
            "Moonwake Revenant",
            25,
            3930,
            750,
            100,
            240,
            30000000,
            null),
        stage("pale_cantor", CHRONICLER, "The Pale Cantor", 1, 4050, 675, 70, 300, 40000000, "threnody_reach",
            "item.moonwake_bell_shard"),
        stage(
            "emberglass_oath",
            CHRONICLER,
            "Emberglass Ashguard",
            30,
            4360,
            2620,
            110,
            240,
            30000000,
            null),
        stage(
            "cinder_marshal",
            CHRONICLER,
            "The Cinder Marshal",
            1,
            4360,
            2630,
            75,
            300,
            40000000,
            "threnody_reach"));
  }

  /** Existing quest statuses are the durable branch choice, including saves from the linear chain. */
  public static List<QuestDef> selectedPath(Player player) {
    List<QuestDef> quests = avalon();
    // Old saves may have finished Moonwake and already accepted Emberglass. Keep that work playable.
    if (quests.subList(2, 4).stream().anyMatch(q -> QuestService.statusFor(player, q) != 0)) {
      return quests.subList(2, 4);
    }
    if (quests.subList(0, 2).stream().anyMatch(q -> QuestService.statusFor(player, q) != 0)) {
      return quests.subList(0, 2);
    }
    return List.of();
  }

  public static boolean accountCompleted(Player player) {
    List<QuestDef> quests = avalon();
    return QuestService.statusFor(player, quests.get(1)) == QuestService.STATUS_COMPLETED
        || QuestService.statusFor(player, quests.get(3)) == QuestService.STATUS_COMPLETED;
  }

  public static final String MOONWAKE_CLUE = "hollow_dawn.clue.moonwake";
  public static final String EMBERGLASS_CLUE = "hollow_dawn.clue.emberglass";

  /** Only unstarted lieutenants need the new witness step; old active saves keep their objective. */
  public static String missingClue(Player player, QuestDef quest) {
    if (QuestService.statusFor(player, quest) != QuestService.STATUS_NOT_STARTED) return null;
    return switch (quest.getId()) {
      case "pale_cantor" -> player.getQuestFlag(MOONWAKE_CLUE) == 0 ? "moonwake" : null;
      case "cinder_marshal" -> player.getQuestFlag(EMBERGLASS_CLUE) == 0 ? "emberglass" : null;
      default -> null;
    };
  }

  public static List<QuestDef> threnody() {
    return List.of(
        stage(
            "ashbound_exiles", KEEPER, "Ashbound Exile", 35, 5690, 1410, 100, 315, 45000000, null),
        stage("hush_cantor", KEEPER, "The Hush Cantor", 1, 5750, 1500, 80, 340, 55000000, null),
        stage("nullguard_watch", KEEPER, "Nullguard", 40, 5470, 1760, 115, 355, 65000000, null),
        stage("dusk_regent", KEEPER, "The Dusk Regent", 1, 5860, 2040, 80, 375, 75000000, null,
            "item.last_witness_seal"),
        stage(
            "rift_unbinding",
            KEEPER,
            "Rift Wraith",
            45,
            5630,
            2100,
            100,
            390,
            85000000,
            "hollow_dawn"),
        stage(
            "rhunor_hollow_dawn",
            KEEPER,
            "Rhunor, the Hollow Dawn",
            1,
            5650,
            2290,
            65,
            400,
            100000000,
            null));
  }

  public static List<QuestDef> all() {
    return java.util.stream.Stream.concat(avalon().stream(), threnody().stream()).toList();
  }

  private static QuestDef stage(
      String id,
      String giver,
      String monster,
      int kills,
      int x,
      int y,
      int radius,
      int minLevel,
      int rewardXp,
      String unlock) {
    return stage(id, giver, monster, kills, x, y, radius, minLevel, rewardXp, unlock, null);
  }

  private static QuestDef stage(
      String id,
      String giver,
      String monster,
      int kills,
      int x,
      int y,
      int radius,
      int minLevel,
      int rewardXp,
      String unlock,
      String requiredItem) {
    String prefix = "${quest." + id;
    return new QuestDef(
        id,
        prefix + ".title}",
        giver,
        monster,
        kills,
        0,
        x,
        y,
        radius,
        1000000,
        rewardXp,
        prefix + ".offer}",
        prefix + ".completion}",
        prefix + ".completed}",
        null,
        requiredItem,
        requiredItem == null ? 0 : 1,
        unlock,
        null,
        minLevel,
        prefix + ".walkthrough}");
  }
}
