package com.perso.T4C.quest.definition;

import com.perso.T4C.quest.QuestDef;

/** A short Dragonguard trial that opens the isolated three-boss island. */
public final class ShatteredIslePassage {
  private ShatteredIslePassage() {}

  public static QuestDef definition() {
    return new QuestDef(
        "shattered_isle_passage",
        "${quest.shattered_isle_passage.title}",
        "OutriderKaelis",
        "Kraanian Dragonguard",
        3,
        0,
        2850,
        2780,
        180,
        25000,
        5000000,
        "${quest.shattered_isle_passage.offer}",
        "${quest.shattered_isle_passage.completion}",
        "${quest.shattered_isle_passage.completed}",
        null,
        null,
        0,
        "shattered_isle",
        null,
        0,
        "${quest.shattered_isle_passage.walkthrough}");
  }
}
