package com.perso.T4C.quest.definition;

import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestService;
import java.util.List;

/** A testimony-and-ritual route to Hollow Dawn, independent of the legacy hunting campaign. */
public final class ConcordCampaign {
  public static final String GIVER = "ArchivistAveline";
  public static final List<String> STYLES =
      List.of("warrior", "archer", "fire", "water", "earth", "air", "light", "dark");
  private static final List<String> IDS =
      List.of(
          "concord_charter",
          "concord_drowned",
          "concord_embers",
          "concord_refuge",
          "concord_resonance",
          "concord_regent",
          "concord_three_bells",
          "concord_dawn");
  private static final List<QuestDef> QUESTS = build();

  private ConcordCampaign() {}

  public static List<QuestDef> all() {
    return QUESTS;
  }

  public static int chapter(Player player) {
    for (int i = 0; i < QUESTS.size(); i++)
      if (QuestService.statusFor(player, QUESTS.get(i)) != QuestService.STATUS_COMPLETED) return i;
    return QUESTS.size();
  }

  public static String flag(int chapter, String step) {
    return "concord." + IDS.get(chapter) + "." + step;
  }

  public static boolean choiceReward(int chapter) {
    return chapter >= 0 && chapter < QUESTS.size();
  }

  public static String reward(int chapter, String style) {
    if (chapter != 2 && chapter != 5 && chapter != 7) {
      return "item.concord_"
          + List.of(
                  "oath_ring",
                  "tide_bracelet",
                  "",
                  "refuge_necklace",
                  "bell_mantle",
                  "",
                  "dawn_signet")
              .get(chapter)
          + "_"
          + style;
    }
    int tier = chapter == 2 ? 1 : chapter == 5 ? 2 : 3;
    return "item.concord_" + tier + "_" + style;
  }

  private static List<QuestDef> build() {
    int[] levels = {200, 230, 260, 300, 330, 375, 390, 395};
    int[] xp = {
      30000000, 50000000, 75000000, 100000000, 150000000, 200000000, 175000000, 250000000
    };
    String[] rewards = {
      "oath_ring_warrior",
      "tide_bracelet_warrior",
      "1_warrior",
      "refuge_necklace_warrior",
      "bell_mantle_warrior",
      "2_warrior",
      "dawn_signet_warrior",
      "3_warrior"
    };
    java.util.ArrayList<QuestDef> result = new java.util.ArrayList<>();
    for (int i = 0; i < IDS.size(); i++) {
      String id = IDS.get(i), prefix = "${quest." + id;
      boolean boss = i == 5 || i == 7;
      result.add(
          new QuestDef(
              id,
              prefix + ".title}",
              GIVER,
              i == 5 ? "The Dusk Regent" : i == 7 ? "Rhunor, the Hollow Dawn" : null,
              boss ? 1 : 0,
              0,
              i == 5 ? 5860 : 5650,
              i == 5 ? 2040 : 2290,
              80,
              (i + 1) * 150000,
              xp[i],
              prefix + ".offer}",
              prefix + ".completion}",
              prefix + ".completed}",
              null,
              boss ? null : "item." + id + "_proof",
              boss ? 0 : 1,
              i == 2 ? "threnody_reach" : i == 6 ? "hollow_dawn" : null,
              "item.concord_" + rewards[i],
              levels[i],
              prefix + ".walkthrough}"));
    }
    return List.copyOf(result);
  }
}
