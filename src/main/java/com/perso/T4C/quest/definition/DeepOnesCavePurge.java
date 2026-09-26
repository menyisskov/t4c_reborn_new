package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

// Deep One is real, pre-existing legacy content (see monster/DeepOne.java) already spawned at 24
// WORLDMAP points clustered around (330, 2246) — this quest's area targets that real cluster
// (centroid + a radius comfortably covering every existing point) rather than an invented
// location, so kills actually register. See docs/content-ideas/2026-09-canon-verified-additions.md.
public final class DeepOnesCavePurge {
  private DeepOnesCavePurge() {}

  public static QuestDef definition() {
    return new QuestDef(
        "deep_ones_cave_purge",
        "${quest.deep_ones_cave_purge.title}",
        "KeeperTamsin",
        "Deep One",
        18,
        0,
        330,
        2246,
        90,
        1600,
        4200,
        "${quest.deep_ones_cave_purge.offer}",
        "${quest.deep_ones_cave_purge.completion}",
        "${quest.deep_ones_cave_purge.completed}",
        null,
        "depths_wardens_talisman",
        1,
        "deep_ones_cave",
        null,
        0,
        "${quest.deep_ones_cave_purge.walkthrough}");
  }
}
